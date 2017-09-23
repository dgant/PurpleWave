package Planning.Plans.Information

import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}
import Strategery.Strategies.Strategy

class Employ(
  strategy        : Strategy,
  implementation  : Plan,
  alternative     : Plan = NoPlan()) extends If(
    new Employing(strategy),
    implementation,
    alternative)
