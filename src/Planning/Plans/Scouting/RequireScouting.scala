package Planning.Plans.Scouting

import Planning.Plans.Compound.{If, Not}

class RequireScouting extends If(
  new Not(new FoundEnemyBase),
  new FindEnemyBase) {
  
  description.set("Find an enemy base")
}
