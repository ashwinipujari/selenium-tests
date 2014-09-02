package com.wikia.webdriver.PageObjectsFactory.ComponentObject.VisualEditorDialogs;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.wikia.webdriver.Common.Core.Assertion;
import com.wikia.webdriver.Common.Logging.PageObjectLogging;

/**
 * @author Robert 'rochan' Chan
 */
public class VisualEditorReviewChangesDialog extends VisualEditorDialog {

	@FindBy(css=".secondary .oo-ui-labeledElement-label")
	private WebElement reviewChangesButton;
	@FindBy(css=
		".oo-ui-window-foot " +
		"div:not(.oo-ui-flaggableElement-secondary):not(.oo-ui-flaggableElement-constructive) " +
		".oo-ui-labeledElement-label")
	private WebElement returnToSaveFormButton;
	@FindBy(css=".ve-ui-mwSaveDialog-viewer.WikiaArticle")
	private WebElement wikiaArticle;
	@FindBy(css=".ve-ui-mwSaveDialog-viewer.WikiaArticle pre")
	private WebElement wikiaAritlceFirstPreview;
	@FindBy(css=".ve-ui-mwSaveDialog-viewer.WikiaArticle")
	private WebElement wikiaArticleDiffTable;
	@FindBy(css=".diff-addedline")
	private List<WebElement> addedLines;
	@FindBy(css=".diff-deletedline")
	private List<WebElement> deletedLines;

	private final int DELETE = 0;
	private final int INSERT = 1;

	public VisualEditorReviewChangesDialog(WebDriver driver) {
		super(driver);
	}

	public VisualEditorSaveChangesDialog clickReturnToSaveFormButton() {
		switchToIFrame();
		waitForElementClickableByElement(returnToSaveFormButton);
		returnToSaveFormButton.click();
		PageObjectLogging.log("clickReturnToSaveFormButton", "Return To Save Form button clicked", true);
		switchOutOfIFrame();
		return new VisualEditorSaveChangesDialog(driver);
	}

	public void verifyDeletedDiffs(ArrayList<String> targets) {
		switchToIFrame();
		verifyArticleDiffs(targets, DELETE);
		switchOutOfIFrame();
	}

	public void verifyAddedDiffs(ArrayList<String> targets) {
		switchToIFrame();
		if (checkIfElementOnPage(wikiaAritlceFirstPreview)) {
			verifyNewArticleDiffs(targets);
		} else {
			verifyArticleDiffs(targets, INSERT);
		}
		switchOutOfIFrame();
	}

	private void verifyArticleDiffs(ArrayList<String> targets, int mode) {
		int count = 0;
		int expectedCount = 0;
		List<WebElement> lines = null;
		if (mode == DELETE) {
			expectedCount = deletedLines.size();
			lines = deletedLines;
		} else if (mode == INSERT) {
			expectedCount = addedLines.size();
			lines = addedLines;
		}
		for (WebElement current : lines) {
			String currentText = current.getText();
			if (!currentText.isEmpty()) {
				if(foundAddedDiff(targets, currentText)) {
					targets.remove(currentText);
					count++;
				}
			} else {
				expectedCount--;
			}
		}
		Assertion.assertNumber(expectedCount, count, "Number of diffs.");
		if (mode == INSERT) {
			Assertion.assertNumber(0, targets.size(), "Number of diffs.");
		}
	}

	private void verifyNewArticleDiffs(ArrayList<String> targets) {
		String wikiText = wikiaAritlceFirstPreview.getText();
		for (String target : targets) {
			verifyNewArticleDiff(target, wikiText);
		}
	}

	private void verifyNewArticleDiff(String target, String source) {
		Assertion.assertStringContains(target, source);
	}

	private boolean foundAddedDiff(ArrayList<String> targets, String source) {
		for (String target : targets) {
			if (source.contains(target))
				return true;
		}
		return false;
	}
}