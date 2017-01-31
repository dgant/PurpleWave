package Types.Plans

class PlanWinTheGame extends PlanParallel {
  override def _onInitialization() {
    super._onInitialization()
    _children = List(
      new PlanFollowBuildOrder,
      new PlanGatherMinerals
    )
  }
}
