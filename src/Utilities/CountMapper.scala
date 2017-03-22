package Utilities

//There must be a more idiomatic Scala way of doing this
object CountMapper {
  def make[T](map:Map[T, Int]):CountMap[T] = {
    val output = new CountMap[T]
    map.keys.foreach(key => output.put(key, map(key)))
    output
  }
}
