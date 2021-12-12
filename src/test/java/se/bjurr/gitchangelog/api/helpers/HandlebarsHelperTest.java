package se.bjurr.gitchangelog.api.helpers;

import static se.bjurr.gitchangelog.api.GitChangelogApi.gitChangelogApiBuilder;

import org.junit.Before;
import org.junit.Test;
import se.bjurr.gitchangelog.api.GitChangelogApi;
import se.bjurr.gitchangelog.test.ApprovalsWrapper;

public class HandlebarsHelperTest {
  private GitChangelogApi baseBuilder;

  @Before
  public void before() {
    this.baseBuilder =
        gitChangelogApiBuilder() //
            .withFromRepo(".") //
            .withFromCommit("1edc0d7") //
            .withToCommit("e78a62f");
  }

  @Test
  public void testThatHelperCanBeSuppliedWithJavascriptFile() throws Exception {
    final GitChangelogApi given =
        this.baseBuilder //
            .withTemplatePath(
                "templatetest/helpers/testThatHelperCanBeSuppliedWithJavascript.mustache")
            .withHandlebarsHelperFile("templatetest/helpers/helper.js");

    ApprovalsWrapper.verify(given);
  }

  @Test
  public void testThatHelperCanBeSuppliedWithJavascript() throws Exception {
    final GitChangelogApi given =
        this.baseBuilder //
            .withTemplatePath(
                "templatetest/helpers/testThatHelperCanBeSuppliedWithJavascript.mustache")
            .withHandlebarsHelper(
                "Handlebars.registerHelper(\"firstWord\", function(options) {"
                    + "  return options.fn(this).split(\" \")[0];"
                    + "});");

    ApprovalsWrapper.verify(given);
  }

  @Test
  public void testThatBuiltInHelperMethodsCanBeUsed() throws Exception {
    final GitChangelogApi given =
        this.baseBuilder //
            .withFromCommit("ed95e6a") //
            .withToCommit("65cfb90") //
            .withTemplatePath(
                "templatetest/helpers/testThatBuiltInHelperMethodsCanBeUsed.mustache");

    ApprovalsWrapper.verify(given);
  }

  @Test
  public void testThatConventionalChangelogCanBeRendered() throws Exception {
    final GitChangelogApi given =
        this.baseBuilder //
            .withFromCommit("ed95e6a") //
            .withToCommit("65cfb90") //
            .withTemplatePath(
                "templatetest/helpers/testThatConventionalChangelogCanBeRendered.mustache");

    ApprovalsWrapper.verify(given);
  }
}
