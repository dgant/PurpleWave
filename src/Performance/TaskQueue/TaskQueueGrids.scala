package Performance.TaskQueue
import Information.Grids.AbstractGrid
import Lifecycle.With
import Performance.Tasks._

class GridTask[T](name: String, grid: AbstractGrid[T]) extends SimpleTask(grid.update) {
  withName(f"Grid$name")
}

class TaskQueueGrids extends TaskQueueParallel(
  new GridTask("Buildable",           With.grids.buildable)                    .withWeight(10),
  new GridTask("BuildableTerrain",    With.grids.buildableTerrain)             .withWeight(10),
  new GridTask("BuildableTownHall",   With.grids.buildableTownHall)            .withWeight(1),
  new GridTask("LastSeen",            With.grids.lastSeen)                     .withWeight(10),
  new GridTask("PsiStorm",            With.grids.psionicStorm)                 .withWeight(100),
  new GridTask("Psi2Height",          With.grids.psi2Height)                   .withWeight(10),
  new GridTask("Psi3Height",          With.grids.psi3Height)                   .withWeight(10),
  new GridTask("ScoutPathBases",      With.grids.scoutingPathsBases)           .withWeight(1),
  new GridTask("ScoutPathStarts",     With.grids.scoutingPathsStartLocations)  .withWeight(1),
  new GridTask("Walkable",            With.grids.walkable)                     .withWeight(1),
  new GridTask("WalkableTerrain",     With.grids.walkableTerrain)              .withWeight(1)) {
  withName("Grids")
}
