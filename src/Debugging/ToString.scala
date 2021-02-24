package Debugging

object ToString {
  def apply(value: Object): String = {
    var name = getClass.getSimpleName
    if (name.contains("anon")) name = getClass.getSuperclass.getSimpleName
    name.replaceAllLiterally("$", "")
  }
}
