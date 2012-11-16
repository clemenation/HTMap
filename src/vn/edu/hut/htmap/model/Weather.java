package vn.edu.hut.htmap.model;

public class Weather {
	private String weatherCondition;
	private String locationName;
	private int temperatureC;
	private int highTempC;
	private int lowTempC;
	private String relativeHumidity;
	private int windKPH;
	private String iconURLString;
	
	/**
	 * @return the weatherCondition
	 */
	public String getWeatherCondition() {
		return weatherCondition;
	}
	/**
	 * @param weatherCondition the weatherCondition to set
	 */
	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}
	/**
	 * @return the locationName
	 */
	public String getLocationName() {
		return locationName;
	}
	/**
	 * @param locationName the locationName to set
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	/**
	 * @return the temperatureC
	 */
	public int getTemperatureC() {
		return temperatureC;
	}
	/**
	 * @param temperatureC the temperatureC to set
	 */
	public void setTemperatureC(int temperatureC) {
		this.temperatureC = temperatureC;
	}
	/**
	 * @return the highTempC
	 */
	public int getHighTempC() {
		return highTempC;
	}
	/**
	 * @param highTempC the highTempC to set
	 */
	public void setHighTempC(int highTempC) {
		this.highTempC = highTempC;
	}
	/**
	 * @return the lowTempC
	 */
	public int getLowTempC() {
		return lowTempC;
	}
	/**
	 * @param lowTempC the lowTempC to set
	 */
	public void setLowTempC(int lowTempC) {
		this.lowTempC = lowTempC;
	}
	/**
	 * @return the windKPH
	 */
	public int getWindKPH() {
		return windKPH;
	}
	/**
	 * @param windKPH the windKPH to set
	 */
	public void setWindKPH(int windKPH) {
		this.windKPH = windKPH;
	}
	/**
	 * @return the iconURLString
	 */
	public String getIconURLString() {
		return iconURLString;
	}
	/**
	 * @param iconURLString the iconURLString to set
	 */
	public void setIconURLString(String iconURLString) {
		this.iconURLString = iconURLString;
	}
	/**
	 * @return the relativeHumidity
	 */
	public String getRelativeHumidity() {
		return relativeHumidity;
	}
	/**
	 * @param relativeHumidity the relativeHumidity to set
	 */
	public void setRelativeHumidity(String relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}
}
