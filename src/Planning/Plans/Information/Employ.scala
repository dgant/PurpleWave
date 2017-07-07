package Planning.Plans.Information

import Planning.Plan
import Planning.Plans.Compound.If
import Strategery.Strategies.Strategy

class Employ(
  strategy        : Strategy,
  implementation  : Plan,
  alternative     : Plan = new Plan) extends If(
    new Employing(strategy),
    implementation,
    alternative)
