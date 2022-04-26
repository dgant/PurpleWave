package Mathematics.Points

object Directions {
  object Up extends Direction(0, -1)
  object Down extends Direction(0, 1)
  object Left extends Direction(-1, 0)
  object Right extends Direction(1, 0)
  val All: Seq[Direction] = Seq(Up, Down, Left, Right)
}
