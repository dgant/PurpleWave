package Performance.TaskQueue
import Performance.Tasks.AbstractTask
import Performance.Tasks.Grids._

class TaskQueueGrids extends AbstractTaskQueue {
  
  override val tasks: Vector[AbstractTask] = Vector (
    new TaskGridPsi2x2and3x2,
    new TaskGridPsi4x3,
    new TaskGridWalkable,
    new TaskGridWalkableTerrain,
    new TaskGridWalkableUnits,
    new TaskGridBuildable,
    new TaskGridBuildableTerrain,
    new TaskGridUnits,
    new TaskGridAltitudeBonus,
    new TaskGridEnemyDetection,
    new TaskGridEnemyVision,
    new TaskGridMobility,
    new TaskGridDpsEnemyGroundConcussive,
    new TaskGridDpsEnemyGroundExplosive,
    new TaskGridDpsEnemyGroundNormal,
    new TaskGridDpsEnemyAirConcussive,
    new TaskGridDpsEnemyAirExplosive,
    new TaskGridDpsEnemyAirNormal
  )
}
