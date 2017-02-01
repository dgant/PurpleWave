package Types.Traits

trait UnitRequest extends ResourceRequest {
  
  def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]]
}
