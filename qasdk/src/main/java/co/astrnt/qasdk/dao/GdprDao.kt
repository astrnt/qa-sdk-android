package co.astrnt.qasdk.dao

class GdprDao(private var gdpr_complied: Int ? = null,
              var gdpr_text: String? = null,
              var gdpr_aggrement_text: String? = null,
              var compliance: String? = null) {
    val isGdprComplied: Boolean
        get() = gdpr_complied == 1

    fun setGdpr_complied(gdpr_complied: Int) {
        this.gdpr_complied = gdpr_complied
    }

}