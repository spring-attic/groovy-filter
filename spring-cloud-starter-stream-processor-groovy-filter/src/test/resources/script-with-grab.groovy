@Grab(group='org.apache.commons', module='commons-math3', version='3.6')
import org.apache.commons.math3.util.Precision;
Precision.round(payload * 0.638 ,3) * 1000 % 2 == 0;
