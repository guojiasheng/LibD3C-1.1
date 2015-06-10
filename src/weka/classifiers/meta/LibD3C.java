package weka.classifiers.meta;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.core.Capabilities;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import cn.edu.xmu.dm.d3c.core.imDC;
import cn.edu.xmu.dm.d3c.core.imDC1;
import cn.edu.xmu.dm.d3c.core.myClassifier;
import cn.edu.xmu.dm.d3c.sample.BaseClassifiersClustering;
import cn.edu.xmu.dm.d3c.sample.BaseClassifiersEnsemble;
import cn.edu.xmu.dm.d3c.sample.ParallelBaseClassifiersTraining;
import cn.edu.xmu.dm.d3c.utils.InitClassifiers;
import cn.edu.xmu.dm.d3c.utils.InstanceUtil;

public class LibD3C extends RandomizableMultipleClassifiersCombiner implements
		TechnicalInformationHandler, EnvironmentHandler {
	protected static final long serialVersionUID = 1L;
	protected int numClusters = 10;
	protected double TargetCorrectRate = 1.0D;
	protected double Interval = 0.05D;
	// protected int numFolds = 5;

	List<String> nameOfClassifiers = new ArrayList<String>();
	List<String> pathOfClassifiers = new ArrayList<String>();
	List<String> parameterOfCV = new ArrayList<String>();
	List<Classifier> cfsArrayCopy = new ArrayList<Classifier>();
	public static ArrayList<String> Option;
	List<Integer> ensemClassifiers;
	public static Classifier[] cfsArray;

	public static boolean flag_im = false;

	protected Random m_Random = new Random(1000L);
	protected List<String> m_classifiersToLoad = new ArrayList();
	protected List<Classifier> m_preBuiltClassifiers = new ArrayList();
	protected transient Environment m_env = Environment.getSystemWide();

	protected Tag[] TAGS_CIRCLECOMBINEALGORITHM = { new Tag(0, "HCNRR"),
			new Tag(1, "HCRR"), new Tag(2, "EFSS"), new Tag(3, "EBSS") };

	protected int m_SelectiveAlgorithm_Type = 2;
	protected final Tag[] TAGS_SELECTIVEALGORITHM = { new Tag(0, "CC"),
			new Tag(1, "DS"), new Tag(2, "HCNRR"), new Tag(3, "HCRR"),
			new Tag(4, "EFSS"), new Tag(5, "EBSS") };

	protected int m_CircleCombine_Type = 1;

	// protected Tag[] TAGS_RULES = {
	// new Tag(1, "AVG", "Average of Probabilities"),
	// new Tag(2, "PROD", "Product of Probabilities"),
	// new Tag(3, "MAJ", "Majority Voting"),
	// new Tag(4, "MIN", "Minimum Probability"),
	// new Tag(5, "MAX", "Maximum Probability"),
	// new Tag(6, "MED", "Median Voting") };

	protected Tag[] TAGS_RULES = { new Tag(1, "Average of Probabilities"),
			new Tag(2, "Product of Probabilities"),
			new Tag(3, "Majority Voting"), new Tag(4, "Minimum Probability"),
			new Tag(5, "Maximum Probability"), new Tag(6, "Median Voting") };

	protected int m_CombinationRule = 1;

	protected double validatePercent = 0.2;

	protected int m_numExecutionSlots = 1;

	protected String classifiersxml = "classifiers.xml";

	protected int timeOut = 20;

	public int getNumClusters() {
		return numClusters;
	}

	public String getClassifiersFilePath() {
		return classifiersxml;
	}

	public void setClassifiersFilePath(String classifiersxml) {
		this.classifiersxml = classifiersxml;
	}

	public void setNumClusters(int num) {
		numClusters = num;
	}

	public double getTargetCorrectRate() {
		return TargetCorrectRate;
	}

	public void setTargetCorrectRate(double correctrate) {
		TargetCorrectRate = correctrate;
	}

	public double getInterval() {
		return Interval;
	}

	public void setInterval(double interval) {
		Interval = interval;
	}

	public SelectedTag getSelectiveAlgorithm() {
		return new SelectedTag(this.m_SelectiveAlgorithm_Type,
				TAGS_SELECTIVEALGORITHM);
	}

	public void setSelectiveAlgorithm(SelectedTag value) {
		if (value.getTags() == TAGS_SELECTIVEALGORITHM)
			this.m_SelectiveAlgorithm_Type = value.getSelectedTag().getID();
	}

	public SelectedTag getCircleCombineAlgorithm() {
		return new SelectedTag(this.m_CircleCombine_Type,
				TAGS_CIRCLECOMBINEALGORITHM);
	}

	public void setCircleCombineAlgorithm(SelectedTag value) {
		if (value.getTags() == TAGS_CIRCLECOMBINEALGORITHM)
			this.m_CircleCombine_Type = value.getSelectedTag().getID();
	}

	public int getNumExecutionSlots() {
		return m_numExecutionSlots;
	}

	public void setNumExecutionSlots(int m_numExecutionSlots) {
		this.m_numExecutionSlots = m_numExecutionSlots;
	}

	public double getValidationRatio() {
		return validatePercent;
	}

	public void setValidationRatio(double validatePercent) {
		this.validatePercent = validatePercent;
	}

	public SelectedTag getEnsembleVotingRule() {
		return new SelectedTag(this.m_CombinationRule, TAGS_RULES);
	}

	public void setEnsembleVotingRule(SelectedTag value) {
		if (value.getTags() == TAGS_RULES)
			this.m_CombinationRule = value.getSelectedTag().getID();
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public String globalInfo() {
		return "Class for combining classifiers. Different combinations of probability estimates for classification are available.\n\nFor more information see:\n\n"
				+ getTechnicalInformation().toString();
	}

	public Enumeration listOptions() {
		Vector result = new Vector();

		// Enumeration enm = super.listOptions();
		// String name = "B";
		//
		// while (enm.hasMoreElements()) {
		// Option opt = (Option)enm.nextElement();
		//
		// if (opt.name().equals(name))
		// continue;
		// result.addElement(opt);
		// }

		result.addElement(new Option("\tRandom number seed.\n"
				+ "\t(default 1)", "S", 1, "-S <num>"));
		result.add(new Option("\tTarget correct rate", "target-correct-rate",
				1, "-target-correct-rate" + " 1.0"));

		result.add(new Option("\tClassifiers file's path", "classifiers-xml",
				1, "-classifiers-xml"
						+ " C:/Program Files/Weka-3-7/classifiers.xml"));

		result.add(new Option(
				"\tInterval for the decreasing target correct rate", "I", 1,
				"-I" + " 0.5"));

		result.add(new Option(
				"\tValidation set's proportion respect to Training Set",
				"validation-ratio", 1, "-validation-ratio" + " 0.2"));

		// result.addElement(new
		// Option("\tCombination rule to use\n\t(default: AVG)",
		// "ensemble-vote-rule", 1, "-ensemble-vote-rule " +
		// Tag.toOptionList(TAGS_RULES)));
		result.addElement(new Option(
				"\tCombination rule to use\n\t(default: AVG)",
				"ensemble-vote-rule", 1, "-ensemble-vote-rule " + TAGS_RULES[0]));

		result.add(new Option(
				"\tCluster number for clustering the base classifiers", "K", 1,
				"-K" + " 5"));

		result.addElement(new Option(
				"\tCircle combination algorithm of the ensemble pharse",
				"circle-combination-algorithm", 1,
				"-circle-combination-algorithm "
						+ TAGS_CIRCLECOMBINEALGORITHM[0]));

		result.addElement(new Option(
				"\tSelective algorithm type of the ensemble pharse",
				"selective-algorithm", 1, "-selective-algorithm "
						+ TAGS_SELECTIVEALGORITHM[3]));

		result.add(new Option("\tNumber of execution slots.\n"
				+ "\t(default 1 - i.e. no parallelism)", "num-slots", 1,
				"-num-slots <num>"));

		result.add(new Option(
				"\tMaximum minutes to train each base classifier", "time-out",
				1, "-time-out 2"));

		return result.elements();
	}

	public String[] getOptions() {

		String[] superOptions = super.getOptions();
		String[] options = new String[superOptions.length + 20];

		int current = 0;

		options[current++] = "-target-correct-rate";
		options[current++] = "" + this.getTargetCorrectRate();

		options[current++] = "-I";
		options[current++] = "" + this.getInterval();

		options[current++] = "-validation-ratio";
		options[current++] = "" + this.getValidationRatio();

		options[current++] = "-ensemble-vote-rule";
		options[current++] = "" + this.getEnsembleVotingRule();

		options[current++] = "-K";
		options[current++] = "" + this.getNumClusters();

		options[current++] = "-circle-combination-algorithm";
		options[current++] = "" + this.getCircleCombineAlgorithm();

		options[current++] = "-selective-algorithm";
		options[current++] = "" + this.getSelectiveAlgorithm();

		options[current++] = "-num-slots";
		options[current++] = "" + this.getNumExecutionSlots();

		options[current++] = "-classifiers-xml";
		options[current++] = "" + this.getClassifiersFilePath();

		options[current++] = "-time-out";
		options[current++] = "" + this.getTimeOut();

		System.arraycopy(superOptions, 0, options, current, superOptions.length);

		current += superOptions.length;
		while (current < options.length) {
			options[current++] = "";
		}

		return options;
	}

	public void setOptions(String[] options) throws Exception {
		this.setTargetCorrectRate(Double.parseDouble(Utils.getOption(
				"target-correct-rate", options)));
		this.setInterval(Double.parseDouble(Utils.getOption("I", options)));
		this.setValidationRatio(Double.parseDouble(Utils.getOption(
				"validation-ratio", options)));
		this.setEnsembleVotingRule(new SelectedTag(Utils.getOption(
				"ensemble-vote-rule", options), TAGS_RULES));
		this.setNumClusters(Integer.parseInt(Utils.getOption("K", options)));
		this.setCircleCombineAlgorithm(new SelectedTag(Utils.getOption(
				"circle-combination-algorithm", options),
				TAGS_CIRCLECOMBINEALGORITHM));
		this.setSelectiveAlgorithm(new SelectedTag(Utils.getOption(
				"selective-algorithm", options), TAGS_SELECTIVEALGORITHM));
		this.setNumExecutionSlots(Integer.parseInt(Utils.getOption("num-slots",
				options)));
		this.setClassifiersFilePath(Utils.getOption("classifiers-xml", options));
		this.setTimeOut(Integer.parseInt(Utils.getOption("time-out", options)));

		String seed = Utils.getOption('S', options);
		if (seed.length() != 0) {
			setSeed(Integer.parseInt(seed));
		} else {
			setSeed(1);
		}
	}

	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result = new TechnicalInformation(
				TechnicalInformation.Type.ARTICLE);
		result.setValue(TechnicalInformation.Field.AUTHOR,
				"Chen Lin, Wenqiang Chen, Cheng Qiu, Yunfeng Wu, Sridhar Krishnan, Quan Zou");
		result.setValue(TechnicalInformation.Field.TITLE,
				"LibD3C: Ensemble classifiers with a clustering and dynamic selection strategy");
		result.setValue(TechnicalInformation.Field.YEAR, "2013");
		result.setValue(TechnicalInformation.Field.JOURNAL, "Neurocomputing");
		result.setValue(TechnicalInformation.Field.VOLUME, "123");
		result.setValue(TechnicalInformation.Field.PAGES, " 424–435");

		return result;
	}

	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		if (this.m_preBuiltClassifiers.size() > 0) {
			if (this.m_Classifiers.length == 0) {
				result = (Capabilities) ((Classifier) this.m_preBuiltClassifiers
						.get(0)).getCapabilities().clone();
			}
			for (int i = 1; i < this.m_preBuiltClassifiers.size(); i++) {
				result.and(((Classifier) this.m_preBuiltClassifiers.get(i))
						.getCapabilities());
			}

			for (Capabilities.Capability cap : Capabilities.Capability.values()) {
				result.enableDependency(cap);
			}

		}

		if ((this.m_CombinationRule == 2) || (this.m_CombinationRule == 3)) {
			result.disableAllClasses();
			result.disableAllClassDependencies();
			result.enable(Capabilities.Capability.NOMINAL_CLASS);
			result.enableDependency(Capabilities.Capability.NOMINAL_CLASS);
		} else if (this.m_CombinationRule == 6) {
			result.disableAllClasses();
			result.disableAllClassDependencies();
			result.enable(Capabilities.Capability.NUMERIC_CLASS);
			result.enableDependency(Capabilities.Capability.NUMERIC_CLASS);
		}

		return result;
	}

	public void buildClassifier(Instances data) throws Exception {
		Instances newData = new Instances(data);
		newData.deleteWithMissingClass();

		this.m_Random = new Random(1000L);

		cfsArray = cfsArrayCopy.toArray(new Classifier[cfsArrayCopy.size()]);
		ParallelBaseClassifiersTraining bct = new ParallelBaseClassifiersTraining();
		data.setClassIndex(data.numAttributes() - 1);
		if (!this.flag_im) {
			List<Classifier> bcfs = bct.trainingBaseClassifiers(data, cfsArray,
					validatePercent, m_numExecutionSlots, timeOut,
					pathOfClassifiers, parameterOfCV);

			BaseClassifiersClustering bcc = new BaseClassifiersClustering();
			String pathPrefix = "";
			String fchooseClassifiers = pathPrefix + "chooseClassifiers.txt";
			List<Integer> chooseClassifiers = bcc.clusterBaseClassifiers(
					fchooseClassifiers, this.numClusters);
			BaseClassifiersEnsemble bce = new BaseClassifiersEnsemble();
			ensemClassifiers = bce.EnsembleClassifiers(data,
					m_SelectiveAlgorithm_Type, m_CircleCombine_Type);
			this.m_Classifiers = null;
			this.m_Classifiers = new Classifier[ensemClassifiers.size()];
			int mIndex = 0;
			for (int i : ensemClassifiers) {
				this.m_Classifiers[mIndex++] = bcfs.get(i);
			}
		} else {
			imDC1 imdc = new imDC1();
			imdc.getBestClassifier(newData);
			myClassifier myclassifier = new myClassifier(newData,
					imdc.bestClassifiers, imdc.lessLabelNum, imdc.lessLabel,
					cfsArray.length);
			myclassifier.initmyclassifier();
			Classifier[] bc = myclassifier.build(newData);
			this.m_Classifiers = null;
			this.m_Classifiers = new Classifier[bc.length];
			int mIndex = 0;
			this.m_Classifiers = bc;
		}

		/*
		 * List<Classifier> bcfs = bct.trainingBaseClassifiers(data, cfsArray,
		 * validatePercent, m_numExecutionSlots, timeOut, pathOfClassifiers,
		 * parameterOfCV);
		 * 
		 * BaseClassifiersClustering bcc = new BaseClassifiersClustering();
		 * String pathPrefix = ""; String fchooseClassifiers = pathPrefix +
		 * "chooseClassifiers.txt"; List<Integer> chooseClassifiers =
		 * bcc.clusterBaseClassifiers( fchooseClassifiers, this.numClusters);
		 * 
		 * data.setClassIndex(data.numAttributes() - 1);
		 */
		// BaseClassifiersEnsemble bce = new BaseClassifiersEnsemble();
		// ensemClassifiers = bce.EnsembleClassifiers(data,
		// m_SelectiveAlgorithm_Type, m_CircleCombine_Type);

		/*
		 * for (int i : ensemClassifiers) { this.m_Classifiers[mIndex++] =
		 * bcfs.get(i); }
		 */
		// save memory
		data = null;
	}

	public double classifyInstance(Instance instance) throws Exception {
		double result;

		switch (this.m_CombinationRule) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			double[] dist = distributionForInstance(instance);

			if (instance.classAttribute().isNominal()) {
				int index = Utils.maxIndex(dist);

				if (dist[index] == 0.0D)
					result = Utils.missingValue();
				else
					result = index;
			} else {

				if (instance.classAttribute().isNumeric())
					result = dist[0];
				else
					result = Utils.missingValue();
			}
			break;
		case 6:
			result = classifyInstanceMedian(instance);
			break;
		default:
			throw new IllegalStateException("Unknown combination rule '"
					+ this.m_CombinationRule + "'!");
		}

		return result;
	}

	protected double classifyInstanceMedian(Instance instance) throws Exception {
		double[] results = new double[this.m_Classifiers.length
				+ this.m_preBuiltClassifiers.size()];

		for (int i = 0; i < this.m_Classifiers.length; i++) {
			results[i] = this.m_Classifiers[i].classifyInstance(instance);
		}
		for (int i = 0; i < this.m_preBuiltClassifiers.size(); i++)
			results[(i + this.m_Classifiers.length)] = ((Classifier) this.m_preBuiltClassifiers
					.get(i)).classifyInstance(instance);
		double result;

		if (results.length == 0) {
			result = 0.0D;
		} else {

			if (results.length == 1)
				result = results[0];
			else
				result = Utils.kthSmallestValue(results, results.length / 2);
		}
		return result;
	}

	public double[] distributionForInstance(Instance instance) throws Exception {
		double[] result = new double[instance.numClasses()];

		switch (this.m_CombinationRule) {
		case 1:
			result = distributionForInstanceAverage(instance);
			break;
		case 2:
			result = distributionForInstanceProduct(instance);
			break;
		case 3:
			result = distributionForInstanceMajorityVoting(instance);
			break;
		case 4:
			result = distributionForInstanceMin(instance);
			break;
		case 5:
			result = distributionForInstanceMax(instance);
			break;
		case 6:
			result[0] = classifyInstance(instance);
			break;
		default:
			throw new IllegalStateException("Unknown combination rule '"
					+ this.m_CombinationRule + "'!");
		}

		if ((!instance.classAttribute().isNumeric())
				&& (Utils.sum(result) > 0.0D)) {
			Utils.normalize(result);
		}
		return result;
	}

	protected double[] distributionForInstanceAverage(Instance instance)
			throws Exception {

		double[] probs = this.m_Classifiers.length > 0 ? getClassifier(0)
				.distributionForInstance(instance)
				: ((Classifier) this.m_preBuiltClassifiers.get(0))
						.distributionForInstance(instance);

		for (int i = 1; i < this.m_Classifiers.length; i++) {
			double[] dist = getClassifier(i).distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				probs[j] += dist[j];
			}
		}

		int index = this.m_Classifiers.length > 0 ? 0 : 1;
		for (int i = index; i < this.m_preBuiltClassifiers.size(); i++) {
			double[] dist = ((Classifier) this.m_preBuiltClassifiers.get(i))
					.distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				probs[j] += dist[j];
			}
		}

		for (int j = 0; j < probs.length; j++) {
			probs[j] /= (this.m_Classifiers.length + this.m_preBuiltClassifiers
					.size());
		}
		return probs;
	}

	protected double[] distributionForInstanceProduct(Instance instance)
			throws Exception {
		double[] probs = this.m_Classifiers.length > 0 ? getClassifier(0)
				.distributionForInstance(instance)
				: ((Classifier) this.m_preBuiltClassifiers.get(0))
						.distributionForInstance(instance);

		for (int i = 1; i < this.m_Classifiers.length; i++) {
			double[] dist = getClassifier(i).distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				probs[j] *= dist[j];
			}
		}

		int index = this.m_Classifiers.length > 0 ? 0 : 1;
		for (int i = index; i < this.m_preBuiltClassifiers.size(); i++) {
			double[] dist = ((Classifier) this.m_preBuiltClassifiers.get(i))
					.distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				probs[j] *= dist[j];
			}
		}

		return probs;
	}

	protected double[] distributionForInstanceMajorityVoting(Instance instance)
			throws Exception {
		double[] probs = new double[instance.classAttribute().numValues()];
		double[] votes = new double[probs.length];

		for (int i = 0; i < this.m_Classifiers.length; i++) {
			probs = getClassifier(i).distributionForInstance(instance);
			int maxIndex = 0;
			for (int j = 0; j < probs.length; j++) {
				if (probs[j] > probs[maxIndex]) {
					maxIndex = j;
				}

			}

			for (int j = 0; j < probs.length; j++) {
				if (probs[j] == probs[maxIndex]) {
					votes[j] += 1.0D;
				}
			}
		}
		for (int i = 0; i < this.m_preBuiltClassifiers.size(); i++) {
			probs = ((Classifier) this.m_preBuiltClassifiers.get(i))
					.distributionForInstance(instance);
			int maxIndex = 0;

			for (int j = 0; j < probs.length; j++) {
				if (probs[j] > probs[maxIndex]) {
					maxIndex = j;
				}

			}

			for (int j = 0; j < probs.length; j++) {
				if (probs[j] == probs[maxIndex]) {
					votes[j] += 1.0D;
				}
			}
		}
		int tmpMajorityIndex = 0;
		for (int k = 1; k < votes.length; k++) {
			if (votes[k] > votes[tmpMajorityIndex]) {
				tmpMajorityIndex = k;
			}

		}

		Vector majorityIndexes = new Vector();
		for (int k = 0; k < votes.length; k++) {
			if (votes[k] == votes[tmpMajorityIndex]) {
				majorityIndexes.add(Integer.valueOf(k));
			}
		}
		int majorityIndex = ((Integer) majorityIndexes.get(this.m_Random
				.nextInt(majorityIndexes.size()))).intValue();

		for (int k = 0; k < probs.length; k++)
			probs[k] = 0.0D;
		probs[majorityIndex] = 1.0D;

		return probs;
	}

	protected double[] distributionForInstanceMax(Instance instance)
			throws Exception {
		double[] max = this.m_Classifiers.length > 0 ? getClassifier(0)
				.distributionForInstance(instance)
				: ((Classifier) this.m_preBuiltClassifiers.get(0))
						.distributionForInstance(instance);

		for (int i = 1; i < this.m_Classifiers.length; i++) {
			double[] dist = getClassifier(i).distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				if (max[j] < dist[j]) {
					max[j] = dist[j];
				}
			}
		}
		int index = this.m_Classifiers.length > 0 ? 0 : 1;
		for (int i = index; i < this.m_preBuiltClassifiers.size(); i++) {
			double[] dist = ((Classifier) this.m_preBuiltClassifiers.get(i))
					.distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				if (max[j] < dist[j]) {
					max[j] = dist[j];
				}
			}
		}
		return max;
	}

	protected double[] distributionForInstanceMin(Instance instance)
			throws Exception {
		double[] min = this.m_Classifiers.length > 0 ? getClassifier(0)
				.distributionForInstance(instance)
				: ((Classifier) this.m_preBuiltClassifiers.get(0))
						.distributionForInstance(instance);

		for (int i = 1; i < this.m_Classifiers.length; i++) {
			double[] dist = getClassifier(i).distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				if (dist[j] < min[j]) {
					min[j] = dist[j];
				}
			}
		}
		int index = this.m_Classifiers.length > 0 ? 0 : 1;
		for (int i = index; i < this.m_preBuiltClassifiers.size(); i++) {
			double[] dist = ((Classifier) this.m_preBuiltClassifiers.get(i))
					.distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				if (dist[j] < min[j]) {
					min[j] = dist[j];
				}
			}
		}
		return min;
	}

	public String toString() {
		if (this.m_Classifiers == null) {
			return "Vote: No model built yet.";
		}

		String result = "Vote combines";
		result = result
				+ " the probability distributions of these base learners:\n";
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			result = result + '\t' + getClassifierSpec(i) + '\n';
		}

		for (Classifier c : this.m_preBuiltClassifiers) {
			result = result + "\t" + c.getClass().getName()
					+ Utils.joinOptions(((OptionHandler) c).getOptions())
					+ "\n";
		}

		result = result + "using the '";

		switch (this.m_CombinationRule) {
		case 1:
			result = result + "Average of Probabilities";
			break;
		case 2:
			result = result + "Product of Probabilities";
			break;
		case 3:
			result = result + "Majority Voting";
			break;
		case 4:
			result = result + "Minimum Probability";
			break;
		case 5:
			result = result + "Maximum Probability";
			break;
		case 6:
			result = result + "Median Probability";
			break;
		default:
			throw new IllegalStateException("Unknown combination rule '"
					+ this.m_CombinationRule + "'!");
		}

		result = result + "' combination rule \n";

		return result;
	}

	public String getRevision() {

		return RevisionUtils.extract("$Revision: 7220 $");
	}

	public void setEnvironment(Environment env) {
		this.m_env = env;
	}

	public void ListChange() {
		this.cfsArray = InitClassifiers.init(this.classifiersxml,
				this.nameOfClassifiers, this.pathOfClassifiers,
				this.parameterOfCV);
		this.Option = InitClassifiers.classifiersOption;
		this.m_Classifiers[0] = new weka.classifiers.functions.LibSVM();
		for (int i = 0; i < this.cfsArray.length; i++)
			this.cfsArrayCopy.add(this.cfsArray[i]);

		for (int i = 0; i < this.m_Classifiers.length; i++)
			this.cfsArrayCopy.add(this.m_Classifiers[i]);

	}

	public void printInfo(Evaluation eval) throws Exception {
		System.out.println(eval.toSummaryString());
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());
	}

	public static void main(String[] argv) throws Exception {
		//String[] argv = "-m -p D://bupa.arff D://bupa.arff D://gjs.txt".split(" ");
		String TrainFilePath = null, TestFilePath = null, cvNum = null, resultFilePath = null;
		boolean cross = false;
		boolean predict = false;
		TrainFilePath = argv[0];
		try {
			if (argv[0].equals("-m")) {
				flag_im = true;
				if (argv[1].equals("-c")) {
					cvNum = argv[2];
					cross = true;
					cvNum = argv[2];
					TrainFilePath = argv[3];
				} else if (argv[1].equals("-p")) {
					TrainFilePath = argv[2];
					TestFilePath = argv[3];
					resultFilePath = argv[4];
					predict = true;
				}
			} else {
				if (argv[0].equals("-c")) {
					cvNum = argv[1];
					TrainFilePath = argv[2];
				} else if (argv[0].equals("-p")) {
					TrainFilePath = argv[1];
					TestFilePath = argv[2];
					resultFilePath = argv[3];
					predict = true;
				}
			}

			InstanceUtil iu = new InstanceUtil();
			Instances input = iu.getInstances(TrainFilePath);
			input.setClassIndex(input.numAttributes() - 1);
			BaseClassifiersEnsemble tt = new BaseClassifiersEnsemble();

			LibD3C d3c = new LibD3C();
			d3c.ListChange();
			if (flag_im == true) {
				if (predict) {
					Instances test = iu.getInstances(TestFilePath);
					test.setClassIndex(input.numAttributes() - 1);
					d3c.buildClassifier(input);
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							resultFilePath));
					for (int j = 0; j < test.numInstances(); j++) {
						writer.write(String.valueOf(d3c.classifyInstance(test
								.get(j))) + ",");
						writer.write(String.valueOf(test.get(j).classValue()));
						writer.newLine();
					}
					writer.flush();
					writer.close();
				} else {
					Evaluation eval = new Evaluation(input);
					eval.crossValidateModel(d3c, input,Integer.parseInt(cvNum), new Random(d3c.getSeed()));
					d3c.printInfo(eval);
				}
			} else {
				d3c.ListChange();
				if (predict) {
					Instances test = iu.getInstances(TestFilePath);
					d3c.buildClassifier(input);
					test.setClassIndex(input.numAttributes() - 1);
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							resultFilePath));
					for (int j = 0; j < test.numInstances(); j++) {
						writer.write(String.valueOf(d3c.classifyInstance(test
								.get(j))) + ",");
						writer.write(String.valueOf(test.get(j).classValue()));
						writer.newLine();
					}
					writer.flush();
					writer.close();
				} else {
					Evaluation eval = new Evaluation(input);
					eval.crossValidateModel(d3c, input,
							Integer.parseInt(cvNum), new Random(d3c.getSeed()));
					System.out.println("-------集成的分类器为：------------");

					for (Integer index : tt.classifer) {
						System.out.println(d3c.pathOfClassifiers.get(index)
								+ " " + d3c.Option.get(index));
					}
					d3c.printInfo(eval);
				}
			}
			input = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("命令格式或数据不对");
			e.printStackTrace();
		}

		// runClassifier(new LibD3C(), argv);
	}
}