package org.geogebra.common.kernel;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.statistics.AlgoDistribution;
import org.geogebra.common.util.DoubleUtil;

public class AlgoInverseBinomialMinimumTrials extends AlgoDistribution {
	public static final int MAX_TRIALS = 10000;
	public static final int MAX_ITERATIONS = 10000;

	/**
	 *
	 * @param label the label.
	 * @param c {@link Construction}
	 * @param cumulativePropability .
	 * @param probability .
	 * @param numberOfTrials .
	 */
	public AlgoInverseBinomialMinimumTrials(String label, Construction c,
			GeoNumberValue cumulativePropability, GeoNumberValue probability,
			GeoNumberValue numberOfTrials) {
		super(c, label, cumulativePropability, probability, numberOfTrials, null);

	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[3];
		input[0] = a.toGeoElement(cons);
		input[1] = b.toGeoElement(cons);
		input[2] = c.toGeoElement(cons);
		super.setOutputLength(1);
		setOutput(0, num);
		setDependencies();
	}

	@Override
	public void compute() {
		if (isInvalidArguments()) {
			num.setUndefined();
			return;
		}
		if (input[0].isDefined() && input[1].isDefined()
				&& input[2].isDefined()) {
			try {
				int count = countCumulativeProbabilityAccepted();
				if (count == MAX_ITERATIONS) {
					num.setUndefined();
				} else {
					num.setValue(count);
				}

			} catch (Exception e) {
				num.setUndefined();
			}
		} else {
			num.setUndefined();
		}
	}

	private int countCumulativeProbabilityAccepted() {
		int count = 0;
		while (isCumulativeProbabilityAccepted(count, getTrials())
				&& count < MAX_ITERATIONS) {
			count++;
		}
		return count;
	}

	private int getTrials() {
		return Math.min((int) Math.round(c.getDouble()), MAX_TRIALS);
	}

	private boolean isCumulativeProbabilityAccepted(int n, int trials) {
		BinomialDistribution dist = getBinomialDistribution(n, getProbability());
		return dist.cumulativeProbability(trials) > getCumulativeProbability();
	}

	private double getCumulativeProbability() {
		return a.getDouble();
	}

	private double getProbability() {
		return b.getDouble();
	}

	private boolean isInvalidArguments() {
		return isProbabilityOutOfRange(a)
				|| isProbabilityOutOfRange(b)
				|| isInvalidTrials();
	}

	private boolean isInvalidTrials() {
		double value = c.getDouble();
		if (!DoubleUtil.isInteger(value)) {
			return true;
		}
		return value < 0 || value > MAX_TRIALS;
	}

	private boolean isCumulativeProbabilityOutOfRange(GeoNumberValue probability) {
		double value = probability.getDouble();
		return value < 0 || value > 1;
	}

	private boolean isProbabilityOutOfRange(GeoNumberValue probability) {
		double value = probability.getDouble();
		return value <= 0 || value > 1;
	}

	@Override
	public GetCommand getClassName() {
		return Commands.InverseBinomialMinimumTrials;
	}
}
