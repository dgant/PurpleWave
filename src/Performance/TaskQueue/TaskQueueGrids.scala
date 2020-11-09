package Performance.TaskQueue
import Performance.Tasks._

class TaskQueueGrids extends AbstractTaskQueue {
  
  override val tasks: Vector[AbstractTask] = Vector (
    new TaskGridAltitudeBonus               { urgency = 1    },
    new TaskGridBuildable                   { urgency = 10   },
    new TaskGridBuildableTerrain            { urgency = 10   },
    new TaskGridBuildableTownHall           { urgency = 1    },
    new TaskGridCreepInitial                { urgency = 1    },
    new TaskGridCreep                       { urgency = 1    },
    new TaskGridEnemyDetection              { urgency = 100  },
    new TaskGridEnemyRangeAir               { urgency = 100  },
    new TaskGridEnemyRangeGround            { urgency = 100  },
    new TaskGridEnemyRangeAirGround         { urgency = 100  },
    new TaskGridEnemyVision                 { urgency = 10   },
    new TaskGridEnemyVulnerabilityGround    { urgency = 100  },
    new TaskGridFriendlyDetection           { urgency = 10   },
    new TaskGridFriendlyVision              { urgency = 10   },
    new TaskGridMobilityBorder              { urgency = 1    },
    new TaskGridMobilityTerrain             { urgency = 1    },
    new TaskGridPsionicStorm                { urgency = 100  },
    new TaskGridPsi2x2and3x2                { urgency = 10   },
    new TaskGridPsi4x3                      { urgency = 10   },
    new TaskGridScoutingPathBases           { urgency = 1    },
    new TaskGridScoutingPathStartLocations  { urgency = 1    },
    new TaskGridUnits                       { urgency = 1000; override def maxConsecutiveSkips = 8 },
    new TaskGridWalkable                    { urgency = 1    },
    new TaskGridWalkableTerrain             { urgency = 1    },
    new TaskGridUnwalkableUnits             { urgency = 5    }
  )
}
