package org.pitest.mutationtest.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.util.Glob;
import org.pitest.util.PitError;

public class SettingsFactoryTest {

  private final ReportOptions options = new ReportOptions();

  private final PluginServices plugins = PluginServices.makeForContextLoader();

  private SettingsFactory     testee;

  @Before
  public void setUp() {
    this.testee = new SettingsFactory(this.options, plugins);
  }

  @Test
  public void shouldReturnTheLegacyTestFrameworkPluginWhenNoOtherOnClasspath() {
    assertTrue(testee.getTestFrameworkPlugin() != null);
  }
  
  @Test
  public void shouldReturnANullCoverageExporterWhenOptionSetToFalse() {
    this.options.setExportLineCoverage(false);
    assertTrue(this.testee.createCoverageExporter() instanceof NullCoverageExporter);
  }

  @Test
  public void shouldReturnEngineWhenRequestedEngineIsKnown() {
    assertTrue(this.testee.createEngine() instanceof GregorEngineFactory);
  }

  @Test(expected = PitError.class)
  public void shouldThrowErrorWhenRequestedEngineNotKnown() {
    this.options.setMutationEngine("unknown");
    this.testee.createEngine();
  }
  
  @Test
  public void shouldReturnListenerWhenRequestedListenerIsKnown() {
    this.options.addOutputFormats(Arrays.asList("XML"));
    assertNotNull(this.testee.createListener());
  }

  @Test
  public void shouldSupportXMLAndCSV() {
    this.options.addOutputFormats(Arrays.asList("CSV","XML"));
    assertNotNull(this.testee.createListener());
  }
  
  @Test(expected = PitError.class)
  public void shouldThrowErrorWhenRequestedListenerNotKnown() {
    this.options.addOutputFormats(Arrays.asList("unknown"));
    this.testee.createListener();
  }
  
  @Test
  public void shouldReturnADefaultJavaExecutableWhenNoneIsSpecified() {
    this.options.setJavaExecutable(null);
    File actual = new File(testee.getJavaExecutable().javaExecutable());
    if(System.getProperty("os.name").contains("Windows"))
        actual = new File(actual.getPath() + ".exe");
    assertTrue(actual.exists());
  }
  
  @Test
  public void shouldReturnSpecifiedJavaExecutableWhenOneSet() {
    this.options.setJavaExecutable("foo");
    assertEquals("foo",testee.getJavaExecutable().javaExecutable());
  }
  
  @Test
  public void shouldNotAllowUserToCalculateCoverageForCoreClasses() {
    this.options.setTargetClasses(Glob.toGlobPredicates(Collections
        .singleton("java/Integer")));
    final CoverageOptions actual = this.testee.createCoverageOptions();
    assertFalse(actual.getFilter().apply("java/Integer"));
  }

  @Test
  public void shouldNotAllowUserToCalculateCoverageForCoverageImplementation() {
    this.options.setTargetClasses(Glob.toGlobPredicates(Collections
        .singleton("/org/pitest/coverage")));
    final CoverageOptions actual = this.testee.createCoverageOptions();
    assertFalse(actual.getFilter().apply("org/pitest/coverage"));
  }
  

  @Test(expected = PitHelpError.class)
  public void shouldNotAllowUserToMakePITMutateItself() {
    this.options.setTargetClasses(Glob.toGlobPredicates(Collections
        .singleton("org.pitest.*")));
    this.testee.createCoverageOptions();
  }

  
}
