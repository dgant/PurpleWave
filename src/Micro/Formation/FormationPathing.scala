package Micro.Formation

private[Formation] object FormationPathing {
  // The final tile whose index is at most the bound, without a zipped view.
  def atOrBefore[A](path: Seq[A], index: Int): Option[A] = {
    val length = path.length
    if (length == 0 || index < 0) None else Some(path(Math.min(index, length - 1)))
  }

  // A nonempty suffix has the same last tile as the original path.
  // Direct indexing avoids constructing and traversing a sliced view.
  def lastAfterDrop[A](path: Seq[A], dropped: Int): Option[A] = {
    val length = path.length
    if (length == 0 || dropped >= length) None else Some(path(length - 1))
  }
}
