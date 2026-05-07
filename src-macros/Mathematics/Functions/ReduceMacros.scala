package Mathematics.Functions

import scala.reflect.macros.blackbox

object ReduceMacros {
  def minBy[A: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context)(values: c.Expr[TraversableOnce[A]])(feature: c.Expr[A => B])(ordering: c.Expr[Ordering[B]]): c.Expr[Option[A]] = {
    reduceBy[A, B](c)(values)(feature)(ordering)(isMin = true)
  }

  def maxBy[A: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context)(values: c.Expr[TraversableOnce[A]])(feature: c.Expr[A => B])(ordering: c.Expr[Ordering[B]]): c.Expr[Option[A]] = {
    reduceBy[A, B](c)(values)(feature)(ordering)(isMin = false)
  }

  private def reduceBy[A: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context)(values: c.Expr[TraversableOnce[A]])(feature: c.Expr[A => B])(ordering: c.Expr[Ordering[B]])(isMin: Boolean): c.Expr[Option[A]] = {
    import c.universe._

    val valueType = weakTypeOf[A]
    val scoreType = weakTypeOf[B].dealias

    def generic =
      if (isMin) {
        q"_root_.Mathematics.Maff.minByGeneric[$valueType, $scoreType]($values)($feature)($ordering)"
      } else {
        q"_root_.Mathematics.Maff.maxByGeneric[$valueType, $scoreType]($values)($feature)($ordering)"
      }

    def primitive(zero: Tree, compare: (Tree, Tree) => Tree): Tree = {
      val better = (nextValue: Tree, outputValue: Tree) =>
        if (isMin) compare(nextValue, outputValue) else compare(outputValue, nextValue)

      q"""
        val reduceValues = $values.toIterator
        var reduceFirst = true
        var reduceOutput = null.asInstanceOf[$valueType]
        var reduceOutputValue = $zero
        while (reduceValues.hasNext) {
          val reduceNext = reduceValues.next()
          val reduceNextValue = $feature(reduceNext)
          if (reduceFirst) {
            reduceFirst = false
            reduceOutput = reduceNext
            reduceOutputValue = reduceNextValue
          } else if (${better(q"reduceNextValue", q"reduceOutputValue")}) {
            reduceOutput = reduceNext
            reduceOutputValue = reduceNextValue
          }
        }
        if (reduceFirst) _root_.scala.None else _root_.scala.Some(reduceOutput)
      """
    }

    val tree =
      if (scoreType =:= typeOf[Double]) {
        primitive(q"0.0", (a, b) => q"_root_.java.lang.Double.compare($a, $b) < 0")
      } else if (scoreType =:= typeOf[Float]) {
        primitive(q"0.0f", (a, b) => q"_root_.java.lang.Float.compare($a, $b) < 0")
      } else if (scoreType =:= typeOf[Int]) {
        primitive(q"0", (a, b) => q"$a < $b")
      } else if (scoreType =:= typeOf[Long]) {
        primitive(q"0L", (a, b) => q"$a < $b")
      } else {
        generic
      }

    c.Expr[Option[A]](tree)
  }
}
