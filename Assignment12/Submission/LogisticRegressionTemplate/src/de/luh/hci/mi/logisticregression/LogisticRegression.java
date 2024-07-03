package de.luh.hci.mi.logisticregression;

public class LogisticRegression {

	// Base path for gesture data files
	public final static String basePath = ".." + java.io.File.separator;
	// Feature vector dimension (flattened 3D points plus an additional 1)
	public final static int N = GestureClass.SAMPLE_POINTS_COUNT * 3 + 1;

	// Number of templates to use for training
	public final static int TRAINING_COUNT = 3;

	// Learning rate for gradient descent
	double alpha = 0.91;

	// Number of iterations for training
	int TRAINING_ITERATIONS = 100;

	// Method to train the model and perform cross-validation
	public void trainAndCrossValidate(GestureClass classA, GestureClass classB) {
		classA.gestureId = 0;
		classB.gestureId = 1;

		// Prepare training data
		double[][] trainXs = new double[2 * TRAINING_COUNT][N];
		int[] trainYs = new int[2 * TRAINING_COUNT];
		getTrainingData(classA, 0, TRAINING_COUNT, trainXs, trainYs, 0);
		getTrainingData(classB, 0, TRAINING_COUNT, trainXs, trainYs, TRAINING_COUNT);

		// Train the model
		double[] w = train(trainXs, trainYs);

		// Evaluate accuracy for classA
		int correct = 0;
		System.out.println(classA.name + ":");
		for (int i = TRAINING_COUNT; i < classA.templates.size(); i++) {
			double[] xs = toXs(classA.templates.get(i));
			double h = h(xs, w);
			System.out.printf("%4.2f\n", h);
			if (h < 0.5)
				correct++;
		}
		System.out.printf("correct rate = %4.2f\n", ((double) correct / (classA.templates.size() - TRAINING_COUNT)));

		// Evaluate accuracy for classB
		correct = 0;
		System.out.println(classB.name + ":");
		for (int i = TRAINING_COUNT; i < classB.templates.size(); i++) {
			double[] xs = toXs(classB.templates.get(i));
			double h = h(xs, w);
			System.out.printf("%4.2f\n", h);
			if (h >= 0.5)
				correct++;
		}
		System.out.printf("correct rate = %4.2f\n", ((double) correct / (classB.templates.size() - TRAINING_COUNT)));
	}

	// Convert template points (Point3D) to an array of doubles
	public double[] toXs(Template t) {
		double[] xs = new double[N];
		xs[0] = 1.0;
		int k = 1;
		for (int j = 0; j < GestureClass.SAMPLE_POINTS_COUNT; j++) {
			Point3D p = t.get(j);
			xs[k++] = p.x;
			xs[k++] = p.y;
			xs[k++] = p.z;
		}
		return xs;
	}

	// Generate training data in matrix format
	public void getTrainingData(GestureClass gesture, int startIndexTemplates, int count, double[][] xs, int[] ys,
			int startIndexXs) {
		for (int i = 0; i < count; i++) {
			xs[i + startIndexXs][0] = 1.0;
			int k = 1;
			Template t = gesture.templates.get(i + startIndexTemplates);
			for (int j = 0; j < GestureClass.SAMPLE_POINTS_COUNT; j++) {
				Point3D p = t.get(j);
				xs[i + startIndexXs][k++] = p.x;
				xs[i + startIndexXs][k++] = p.y;
				xs[i + startIndexXs][k++] = p.z;
			}
			ys[i + startIndexXs] = gesture.gestureId;
		}
	}

	// Train the model using gradient descent
	public double[] train(double[][] trainXs, int[] trainYs) {
		int m = trainXs.length;
		int n = trainXs[0].length;
		double[] w = new double[n];

		for (int iter = 0; iter < TRAINING_ITERATIONS; iter++) {
			double[] hx = new double[m];
			for (int i = 0; i < m; i++) {
				hx[i] = h(trainXs[i], w);
			}

			double[] grad = gradJ(trainXs, hx, trainYs);

			for (int j = 0; j < n; j++) {
				w[j] -= alpha * grad[j];
			}
		}
		return w;
	}

	// Calculate the dot product of two vectors
	public double dot(double[] a, double[] b) {
		double dot = 0;
		int n = a.length;
		for (int i = 0; i < n; i++) {
			dot += a[i] * b[i];
		}
		return dot;
	}

	// The logistic function (sigmoid function)
	public double h(double[] x, double[] w) {
		return 1.0 / (1.0 + Math.exp(-dot(x, w)));
	}

	// The cost function for a single point
	public double cost(double hx, int y) {
		if (y == 1) {
			return -Math.log(hx);
		} else {
			return -Math.log(1 - hx);
		}
	}

	// The total cost across training samples
	public double J(double[] hx, int[] y) {
		int m = y.length;
		double totalCost = 0.0;
		for (int i = 0; i < m; i++) {
			totalCost += cost(hx[i], y[i]);
		}
		return totalCost / m;
	}

	// Gradient of the cost function across the training set
	public double[] gradJ(double[][] x, double[] hx, int[] y) {
		int M = x.length;
		int N = x[0].length;
		double[] gradJ = new double[N];
		for (int j = 0; j < N; j++) {
			for (int i = 0; i < M; i++) {
				gradJ[j] += (hx[i] - y[i]) * x[i][j];
			}
			gradJ[j] = gradJ[j] / M;
		}
		return gradJ;
	}

	// Main method to start the program
	public static void main(String[] args) throws Exception {
		GestureClass leftRight = new GestureClass(basePath + "left-right.txt", "Left Right", 0);
		GestureClass circles = new GestureClass(basePath + "circle.txt", "Circles", 1);
		GestureClass leftRightArc = new GestureClass(basePath + "left-right-arc.txt", "Left Right Arc", 2);
		GestureClass infinity = new GestureClass(basePath + "infinity.txt", "Infinity", 3);

		leftRight.resample();
		circles.resample();
		leftRightArc.resample();
		infinity.resample();

		LogisticRegression lr = new LogisticRegression();

		lr.trainAndCrossValidate(leftRight, circles);
		lr.trainAndCrossValidate(leftRight, leftRightArc);
		lr.trainAndCrossValidate(leftRight, infinity);

		lr.trainAndCrossValidate(circles, leftRightArc);
		lr.trainAndCrossValidate(circles, infinity);

		lr.trainAndCrossValidate(leftRightArc, infinity);
	}
}
