package Debugging

object ToString {
  def apply(value: Object): String = {
    var name = value.getClass.getSimpleName
    if (name.contains("anon")) name = value.getClass.getSuperclass.getSimpleName
    name.replaceAllLiterally("$", "")
  }
}
