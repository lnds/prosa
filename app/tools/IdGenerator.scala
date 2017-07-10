package tools

import com.fasterxml.uuid.Generators
import java.util.concurrent.ConcurrentHashMap
import java.util.{UUID => JavaUUID}


// taken from activate-framework by fw-brasil
object UUIDUtil {

  // Create an ids pool? uuid.toString is slow! :(

  private[this] val uuidGenerator = Generators.timeBasedGenerator
  def generateUUID = uuidGenerator.generate.toString
  def timestamp(uuid: String) = (JavaUUID.fromString(uuid).timestamp() - 122192928000000000l) / 10000

}

object IdGenerator {

  def nextId(entityClass: Class[_]) = {
    val uuid = UUIDUtil.generateUUID
    val classId = entityClassHashId(entityClass)
    uuid + "-" + classId
  }

  private[this] def classHashIdsCache = new ConcurrentHashMap[Class[_], String]()

  def entityClassHashId(entityClass: Class[_]): String =
    Option(classHashIdsCache.get(entityClass)) match {
      case Some(hash) => hash
      case None =>
        val hash = entityClassHashId(entityClass.getCanonicalName)
        classHashIdsCache.put(entityClass, hash)
        hash
    }

  private[this] def entityClassHashId(entityName: String): String =
    normalizeHex(Integer.toHexString(entityName.hashCode))


  private[this] def normalizeHex(hex: String) = {
    val length : Int = hex.length
    if (8 == length)
      hex
    else if (length < 8)
      hex + (for (_ <- 0 until (8 - length)) yield "0").mkString("")
    else
      hex.substring(0, 8)
  }
}