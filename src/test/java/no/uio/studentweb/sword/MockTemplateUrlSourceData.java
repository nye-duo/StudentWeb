package no.uio.studentweb.sword;


public class MockTemplateUrlSourceData implements TemplateUrlSourceData
{
	public String getTemplateProperty(String propertyName) throws TemplateUrlPropertyException
	{
		if ("unit-code".equals(propertyName))
		{
			return "sd-uri";
		}
		else if ("other".equals(propertyName))
		{
			return "other";
		}
		throw new TemplateUrlPropertyException("no such key " + propertyName);
	}
}
