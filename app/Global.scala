import play.api.mvc.WithFilters
import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import com.mohiva.play.xmlcompressor.XMLCompressorFilter

object Global extends WithFilters(HTMLCompressorFilter(), XMLCompressorFilter()) {


}