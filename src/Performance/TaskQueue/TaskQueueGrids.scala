package Performance.TaskQueue
import Performance.Tasks._

class TaskQueueGrids extends TaskQueueParallel(
  new TaskGridBuildable                   { urgency = 10   },
  new TaskGridBuildableTerrain            { urgency = 10   },
  new TaskGridBuildableTownHall           { urgency = 1    },
  new TaskGridEnemyDetection              { urgency = 100  },
  new TaskGridEnemyRangeAir               { urgency = 100  },
  new TaskGridEnemyRangeGround            { urgency = 100  },
  new TaskGridEnemyRangeAirGround         { urgency = 100  },
  new TaskGridEnemyVision                 { urgency = 10   },
  new TaskGridFriendlyDetection           { urgency = 10   },
  new TaskGridFriendlyVision              { urgency = 10   },
  new TaskGridPsionicStorm                { urgency = 100  },
  new TaskGridPsi2x2and3x2                { urgency = 10   },
  new TaskGridPsi4x3                      { urgency = 10   },
  new TaskGridScoutingPathBases           { urgency = 1    },
  new TaskGridScoutingPathStartLocations  { urgency = 1    },
  new TaskGridUnits                       { urgency = 1000 },
  new TaskGridWalkable                    { urgency = 1    },
  new TaskGridWalkableTerrain             { urgency = 1    },
  new TaskGridUnwalkableUnits             { urgency = 5    })
