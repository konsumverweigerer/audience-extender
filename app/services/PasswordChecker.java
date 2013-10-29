package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.vt.middleware.password.AlphabeticalSequenceRule;
import edu.vt.middleware.password.CharacterCharacteristicsRule;
import edu.vt.middleware.password.DigitCharacterRule;
import edu.vt.middleware.password.LengthRule;
import edu.vt.middleware.password.LowercaseCharacterRule;
import edu.vt.middleware.password.NonAlphanumericCharacterRule;
import edu.vt.middleware.password.NumericalSequenceRule;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.QwertySequenceRule;
import edu.vt.middleware.password.RepeatCharacterRegexRule;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.RuleResult;
import edu.vt.middleware.password.UppercaseCharacterRule;
import edu.vt.middleware.password.WhitespaceRule;

public class PasswordChecker {
	public static final LengthRule lengthRule = new LengthRule(8, 48);
	public static final WhitespaceRule whitespaceRule = new WhitespaceRule();
	public static final QwertySequenceRule qwertySeqRule = new QwertySequenceRule();
	public static final RepeatCharacterRegexRule repeatRule = new RepeatCharacterRegexRule(
			4);
	public static final AlphabeticalSequenceRule alphaSeqRule = new AlphabeticalSequenceRule();
	public static final NumericalSequenceRule numSeqRule = new NumericalSequenceRule(
			3, false);

	public static final CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
	static {
		charRule.getRules().add(new DigitCharacterRule(1));
		charRule.getRules().add(new NonAlphanumericCharacterRule(1));
		charRule.getRules().add(new UppercaseCharacterRule(1));
		charRule.getRules().add(new LowercaseCharacterRule(1));
		charRule.setNumberOfCharacteristics(3);
	}

	public static boolean checkPassword(String password, int level,
			Collection<String> messages) {
		final List<Rule> ruleList = new ArrayList<Rule>();

		if (level > 1) {
			ruleList.add(lengthRule);
			ruleList.add(whitespaceRule);
		}
		if (level > 3) {
			ruleList.add(qwertySeqRule);
			ruleList.add(repeatRule);
		}
		if (level > 5) {
			ruleList.add(alphaSeqRule);
			ruleList.add(numSeqRule);
		}
		if (level > 7) {
			ruleList.add(charRule);
		}

		final PasswordValidator validator = new PasswordValidator(ruleList);
		final PasswordData passwordData = new PasswordData(new Password(
				password));

		final RuleResult result = validator.validate(passwordData);
		if (result.isValid()) {
			return true;
		} else {
			if (messages != null) {
				for (final String msg : validator.getMessages(result)) {
					messages.add(msg);
				}
			}
		}
		return false;
	}
}
