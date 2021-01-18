package Performance.TaskQueue
import Information.Grids.AbstractGrid
import Lifecycle.With
import Performance.Tasks._

class GridTask[T](grid: AbstractGrid[T]) extends SimpleTask(grid.toString, grid.update)

class TaskQueueGrids extends TaskQueueParallel(
  new GridTask(With.grids.buildable)                    .withWeight(10),
  new GridTask(With.grids.buildableTerrain)             .withWeight(10),
  new GridTask(With.grids.buildableTownHall)            .withWeight(1),
  new GridTask(With.grids.enemyDetection)               .withWeight(100),
  new GridTask(With.grids.enemyRangeAir)                .withWeight(100),
  new GridTask(With.grids.enemyRangeGround)             .withWeight(100),
  new GridTask(With.grids.enemyRangeAirGround)          .withWeight(100),
  new GridTask(With.grids.enemyVision)                  .withWeight(10),
  new GridTask(With.grids.friendlyDetection)            .withWeight(10),
  new GridTask(With.grids.lastSeen)                     .withWeight(10),
  new GridTask(With.grids.psionicStorm)                 .withWeight(100),
  new GridTask(With.grids.psi2Height)                   .withWeight(10),
  new GridTask(With.grids.psi3Height)                   .withWeight(10),
  new GridTask(With.grids.scoutingPathsBases)           .withWeight(1),
  new GridTask(With.grids.scoutingPathsStartLocations)  .withWeight(1),
  new GridTask(With.grids.units)                        .withWeight(1000),
  new GridTask(With.grids.walkable)                     .withWeight(1),
  new GridTask(With.grids.walkableTerrain)              .withWeight(1),
  new GridTask(With.grids.unwalkableUnits)              .withWeight(5))
