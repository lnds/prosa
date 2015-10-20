package tools

import java.util.concurrent.ConcurrentHashMap
import java.util.{UUID => JavaUUID}

import com.fasterxml.uuid.Generators


// taken from activate-framework by fw-brasil
object UUIDUtil {

  // Create an ids pool? uuid.toString is slow! :(

  private val uuidGenerator = Generators.timeBasedGenerator
  def generateUUID = uuidGenerator.generate.toString
  def timestamp(uuid: String) = (JavaUUID.fromString(uuid).timestamp() - 122192928000000000l) / 10000

}

object IdGenerator {

  def nextId(entityClass: Class[_]) = {
    val uuid = UUIDUtil.generateUUID
    val classId = getEntityClassHashId(entityClass)
    uuid + "-" + classId
  }

  private def classHashIdsCache = new ConcurrentHashMap[Class[_], String]()

  def getEntityClassHashId(entityClass: Class[_]): String =
    Option(classHashIdsCache.get(entityClass)) match {
      case Some(hash) => hash
      case None =>
        val hash = getEntityClassHashId(entityClass.getCanonicalName)
        classHashIdsCache.put(entityClass, hash)
        hash
    }

  private def getEntityClassHashId(entityName: String): String =
    normalizeHex(Integer.toHexString(entityName.hashCode))


  private def normalizeHex(hex: String) = {
    val length = hex.length
    if (length == 8)
      hex
    else if (length < 8)
      hex + (for (i <- 0 until (8 - length)) yield "0").mkString("")
    else
      hex.substring(0, 8)
  }
}