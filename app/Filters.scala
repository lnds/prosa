import javax.inject.Inject

import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import com.mohiva.play.xmlcompressor.XMLCompressorFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

/**
 * Filters for HTML Compressor
 * Created by ediaz on 11-10-15.
 */
class Filters @Inject() (htmlCompressorFilter: HTMLCompressorFilter, xmlCompressorFilter: XMLCompressorFilter)
  extends HttpFilters {

  override def filters: Seq[EssentialFilter] = Seq(
    htmlCompressorFilter,
    xmlCompressorFilter
  )
}
