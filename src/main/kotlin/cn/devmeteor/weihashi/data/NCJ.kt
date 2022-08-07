package cn.devmeteor.weihashi.data

data class NCJ(
    var term: String,
    var name: String,
    var total: String,
    var credit: String,
    var duration: String,
    var am: String,
    var ep: String,
    var lp: String
) {
    override fun equals(other: Any?): Boolean {
        val oNCJ = (other as NCJ)
        return oNCJ.name == name
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}