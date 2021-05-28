package Information.Grids

trait Grid {
  def update(): Unit = {}
  var code: String = "uncoded"
  def reprAt(i: Int): String
}
