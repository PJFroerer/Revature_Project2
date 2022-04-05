import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.catalyst.dsl.expressions.StringToAttributeConversionHelper
import org.apache.spark.sql.functions._
object test {
  def main(args: Array[String]): Unit = {
    println("Hello World!")
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark =
      SparkSession
        .builder
        .appName("Hello Spark App")
        //.master("local")
        .config("spark.master", "local")
        .config("spark.eventLog.enabled", false)
        .getOrCreate()


    Logger.getLogger("org").setLevel(Level.INFO)
    spark.sparkContext.setLogLevel("ERROR")
    println("Hello Spark")

    val CovidData = spark.read.format("csv").option("inferSchema", true).options(Map("header"->"true", "delimiter" -> ",")).load("C:/Input/P2/covid_19_data.csv").toDF("ID", "Obsv_Date", "Province_State", "Country_Region", "Updated", "Confirmed", "Deaths", "Recovered")
    val covidDF = CovidData.withColumn("ID",col("ID").cast("Integer"))
      .withColumn("Updated", col("Updated").cast("Timestamp"))
      .withColumn("Confirmed",col("Confirmed").cast("Double"))
      .withColumn("Deaths",col("Deaths").cast("Double"))
      .withColumn("Recovered",col("Recovered").cast("Double"))
      .withColumn("Obsv_Date", to_date(col("Obsv_Date"), "MM/dd/yyyy"))
      .persist()

    spark.sql("drop table if exists AllCovidData")
    covidDF.write.saveAsTable("AllCovidData")
    val covidDFNN = covidDF.filter(covidDF("Province_State").isNotNull && covidDF("Updated").isNotNull)
    spark.sql("drop table if exists CovidNN")
    covidDFNN.write.saveAsTable("CovidNN")
    val CovidData2 = spark.sql("select * from CovidNN WHERE NOT Province_State = 'Unknown'").show
    spark.stop()


  }
}
