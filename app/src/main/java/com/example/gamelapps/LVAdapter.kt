package com.example.gamelapps

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class LVAdapter (private val context: Activity, var songs :List<Songs>) :ArrayAdapter<Songs>(context,
    R.layout.layout_song_list, songs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.layout_song_list, null, true)
//        val textViewID = listViewItem.findViewById(R.id.tvIDgamelan) as TextView
        val textViewJudul = listViewItem.findViewById(R.id.tvJudul) as TextView
        val textViewPengarang = listViewItem.findViewById(R.id.tvPengarang) as TextView
        val textViewNada = listViewItem.findViewById(R.id.tvNada) as TextView

        val songs = songs[position]
//        textViewID.text = "Song ID : "+ songs.idGamelanNotasi
        textViewJudul.text = "Judul : " + songs.judul
        textViewPengarang.text = "Pengarang : " + songs.pengarang
        textViewNada.text = "Nada Dasar : " + songs.nadaDasar


        return listViewItem
    }
}