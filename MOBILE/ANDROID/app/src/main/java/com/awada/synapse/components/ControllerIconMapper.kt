package com.awada.synapse.components

import com.awada.synapse.R

fun controllerIconResId(icoNum: Int): Int {
    return when (icoNum) {
        100 -> R.drawable.controller_100_default
        101 -> R.drawable.controller_101_ofis
        102 -> R.drawable.controller_102_dom
        103 -> R.drawable.controller_103_banya
        104 -> R.drawable.controller_104_etazh
        105 -> R.drawable.controller_105_etazh1
        106 -> R.drawable.controller_106_etazh2
        107 -> R.drawable.controller_107_etazh3
        108 -> R.drawable.controller_108_kvartira
        109 -> R.drawable.controller_109_kabinety
        110 -> R.drawable.controller_110_garazh
        111 -> R.drawable.controller_111_hoz
        112 -> R.drawable.controller_112_terr
        113 -> R.drawable.controller_113_open
        114 -> R.drawable.controller_114_sklad
        115 -> R.drawable.controller_115_magazin
        116 -> R.drawable.controller_116_kafe
        117 -> R.drawable.controller_117_bolnica
        118 -> R.drawable.controller_118_proizvodstvo
        119 -> R.drawable.controller_119_podval
        120 -> R.drawable.controller_120_cherdak
        121 -> R.drawable.controller_121_parkovka
        122 -> R.drawable.controller_122_konf_zal
        123 -> R.drawable.controller_123_resepshn
        124 -> R.drawable.controller_124_holl
        else -> R.drawable.controller_100_default
    }
}

