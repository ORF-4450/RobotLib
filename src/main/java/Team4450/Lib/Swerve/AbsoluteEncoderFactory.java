package Team4450.Lib.Swerve;

@FunctionalInterface
public interface AbsoluteEncoderFactory<Configuration> 
{
    AbsoluteEncoder create(Configuration configuration);
}
