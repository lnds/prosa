package tools

import com.fasterxml.uuid.Generators
import java.util.concurrent.ConcurrentHashMap
import java.util.{UUID => JavaUUID}


// taken from activate-framework by fw-brasil
object UUIDUtil {

  // Create an ids pool? uuid.toString is slow! :(

  private[this] val uuidGenerator = Generators.timeBasedGenerator
  def generateUUID: String = uuidGenerator.generate.toString
  def timestamp(uuid: String): Long = (JavaUUID.fromString(uuid).timestamp() - 122192928000000000L) / 10000

}

object IdGenerator {

  def nextId(entityClass: Class[_]): String = {
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
    val maxSize : Int = 8
    val length : Int = hex.length
    if (maxSize == length)
      hex
    else if (length < maxSize)
      hex + (for (_ <- 0 until (maxSize - length)) yield "0").mkString("")
    else
      hex.substring(0, maxSize)
  }
}