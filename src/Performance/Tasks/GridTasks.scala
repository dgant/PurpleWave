package Performance.Tasks

import Information.Grids.AbstractGrid
import Lifecycle.With

abstract class GridTask[T](grid: AbstractGrid[T]) extends TimedTask {
  override protected def onRun(): Unit = grid.update()
}

class TaskGridBuildable                   extends GridTask(With.grids.buildable)
class TaskGridBuildableTerrain            extends GridTask(With.grids.buildableTerrain)
class TaskGridBuildableTownHall           extends GridTask(With.grids.buildableTownHall)
class TaskGridEnemyDetection              extends GridTask(With.grids.enemyDetection)
class TaskGridEnemyRangeAir               extends GridTask(With.grids.enemyRangeAir)
class TaskGridEnemyRangeGround            extends GridTask(With.grids.enemyRangeGround)
class TaskGridEnemyRangeAirGround         extends GridTask(With.grids.enemyRangeAirGround)
class TaskGridEnemyVision                 extends GridTask(With.grids.enemyVision)
class TaskGridFriendlyDetection           extends GridTask(With.grids.friendlyDetection)
class TaskGridFriendlyVision              extends GridTask(With.grids.lastSeen)
class TaskGridPsionicStorm                extends GridTask(With.grids.psionicStorm)
class TaskGridPsi2x2and3x2                extends GridTask(With.grids.psi2Height)
class TaskGridPsi4x3                      extends GridTask(With.grids.psi3Height)
class TaskGridScoutingPathBases           extends GridTask(With.grids.scoutingPathsBases)
class TaskGridScoutingPathStartLocations  extends GridTask(With.grids.scoutingPathsStartLocations)
class TaskGridUnits                       extends GridTask(With.grids.units)
class TaskGridWalkable                    extends GridTask(With.grids.walkable)
class TaskGridWalkableTerrain             extends GridTask(With.grids.walkableTerrain)
class TaskGridUnwalkableUnits             extends GridTask(With.grids.unwalkableUnits)