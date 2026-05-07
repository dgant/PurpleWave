package Utilities

import scala.reflect.macros.blackbox

object TernaryMacros {
  def apply[T: c.WeakTypeTag](c: blackbox.Context)(predicate: c.Expr[Boolean], yes: c.Expr[T], no: c.Expr[T]): c.Expr[T] = {
    import c.universe._
    c.Expr[T](q"if ($predicate) $yes else $no")
  }
}
