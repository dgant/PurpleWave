package Placement.Walls

import Debugging.SimpleString

object WallProblems {
  private var _all: Seq[WallProblem] = Seq.empty
  def all: Seq[WallProblem] = _all

  trait WallProblem           extends SimpleString { _all :+= this }
  object UnbuildableTerrain   extends WallProblem
  object UnbuildableGranular  extends WallProblem
  object IntersectsPrevious   extends WallProblem
  object IntersectsHall       extends WallProblem
  object IntersectsMining     extends WallProblem
  object InsufficientlyTight  extends WallProblem
  object WrongZone            extends WallProblem
  object FailedRecursively    extends WallProblem
  object GapTooNarrow         extends WallProblem
  object GapTooWide           extends WallProblem
  object NoHallway            extends WallProblem
  object InsufficientFiller   extends WallProblem
  object UnpoweredByWall      extends WallProblem
  object UnpoweredByFiller    extends WallProblem
}
