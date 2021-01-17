package Performance.TaskQueue
import Information.Grids.AbstractGrid
import Lifecycle.With
import Performance.Tasks._

class GridTask[T](grid: AbstractGrid[T]) extends TimedTask {
  override protected def onRun(): Unit = grid.update()
}

class TaskQueueGrids extends TaskQueueParallel(
  new GridTask(With.grids.buildable)                    .withUrgency(10),
  new GridTask(With.grids.buildableTerrain)             .withUrgency(10),
  new GridTask(With.grids.buildableTownHall)            .withUrgency(1),
  new GridTask(With.grids.enemyDetection)               .withUrgency(100),
  new GridTask(With.grids.enemyRangeAir)                .withUrgency(100),
  new GridTask(With.grids.enemyRangeGround)             .withUrgency(100),
  new GridTask(With.grids.enemyRangeAirGround)          .withUrgency(100),
  new GridTask(With.grids.enemyVision)                  .withUrgency(10),
  new GridTask(With.grids.friendlyDetection)            .withUrgency(10),
  new GridTask(With.grids.lastSeen)                     .withUrgency(10),
  new GridTask(With.grids.psionicStorm)                 .withUrgency(100),
  new GridTask(With.grids.psi2Height)                   .withUrgency(10),
  new GridTask(With.grids.psi3Height)                   .withUrgency(10),
  new GridTask(With.grids.scoutingPathsBases)           .withUrgency(1),
  new GridTask(With.grids.scoutingPathsStartLocations)  .withUrgency(1),
  new GridTask(With.grids.units)                        .withUrgency(1000),
  new GridTask(With.grids.walkable)                     .withUrgency(1),
  new GridTask(With.grids.walkableTerrain)              .withUrgency(1),
  new GridTask(With.grids.unwalkableUnits)              .withUrgency(5))
