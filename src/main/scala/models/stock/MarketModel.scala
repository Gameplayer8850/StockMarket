package models.stock

case class MarketModel(idMarket: Int, location: String, locationCity: String, timeCestOpen: Option[String], timeCestClose: Option[String])
