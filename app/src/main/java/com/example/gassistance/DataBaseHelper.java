package com.example.gassistance;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "answers.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ANSWERS = "Answers";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_QUESTION = "Question";
    private static final String COLUMN_ANSWER = "Answer";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ANSWERS_TABLE = "CREATE TABLE " + TABLE_ANSWERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_QUESTION + " TEXT,"
                + COLUMN_ANSWER + " TEXT" + ")";
        db.execSQL(CREATE_ANSWERS_TABLE);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
        onCreate(db);
    }

    public void insertAnswer(String question, String answer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUESTION, question);
        values.put(COLUMN_ANSWER, answer);
        db.insert(TABLE_ANSWERS, null, values);
        Log.e("data", String.valueOf(values));
        db.close();
    }

    public static void insertSampleData(SQLiteDatabase db) {
        //db.execSQL("DELETE FROM Answers");
        db.execSQL("INSERT INTO Answers (Question, Answer) VALUES " +
                "('What is artificial intelligence?', 'Artificial intelligence is the simulation of human intelligence processes by computer systems, it can perform tasks like learning, reasoning, perception, understanding, and interaction.'), " +
                "('What is machine learning?', 'Machine learning is a subfield of artificial intelligence that studies how to design and build systems that can learn from data without being explicitly programmed.'), " +
                "('What is deep learning?', 'Deep learning is a branch of machine learning that uses artificial neural networks to model and learn complex representations and patterns.'), " +
                "('What is natural language processing?', 'Natural language processing is a field of artificial intelligence that involves the interaction between computers and human languages, including tasks such as text analysis, speech recognition, and language translation.'), " +
                "('What is data mining?', 'Data mining is the process of discovering useful patterns and information from large datasets, involving techniques such as statistical analysis, machine learning, and database systems.'), " +
                "('What is reinforcement learning?', 'Reinforcement learning is a branch of machine learning that studies how to achieve a goal by observing the environment and taking actions, learning from this process.'), " +
                "('What is a neural network?', 'A neural network is a computing system that mimics the interconnected structure of biological neural networks, consisting of a large number of interconnected artificial neurons, used to simulate the learning process of the human brain.'), " +
                "('What is nuclear energy?', 'Nuclear energy is energy released from atomic nucleus reactions, commonly used for electricity generation and medical applications.'), " +
                "('What is global climate change?', 'Global climate change refers to the long-term changes in the Earths climate system, usually caused by the increase of greenhouse gases in the atmosphere due to human activities.'), " +
                "('What is solar energy?', 'Solar energy is energy from the sun that is converted into usable electricity or heat through photovoltaics or concentrated solar power systems, widely used in the renewable energy sector.'), " +
                "('What is the human genome?', 'The human genome is the total genetic material contained in the DNA of human cells, determining human genetic characteristics and biological functions.'), " +
                "('What is quantum computing?', 'Quantum computing is a computing paradigm that utilizes the principles of quantum mechanics to design computational systems capable of performing faster and more complex computations than traditional computers.'), " +
                "('What is artificial life?', 'Artificial life is a research field that aims to create synthetic life forms by simulating or synthesizing biological life processes and behaviors, studying issues such as the origin and evolution of life.'), " +
                "('What is robotics technology?', 'Robotics technology is the study and development of automated mechanical devices and systems designed to perform specific tasks.'), " +
                "('What is intelligent transportation system?', 'Intelligent transportation system is a system that utilizes advanced information and communication technologies to manage and optimize traffic flow, improving traffic efficiency and safety.'), " +
                "('What is climate change?', 'Climate change refers to the long-term changes in the Earths climate, usually caused by the emission of greenhouse gases into the atmosphere as a result of human activities.'), " +
                "('What is sustainable development?', 'Sustainable development is a development approach that meets the needs of the present generation without compromising the ability of future generations to meet their own needs, emphasizing the balance between economy, society, and environment.'), " +
                "('What is globalization?', 'Globalization is the process of increasing interconnectedness and interdependence among countries in terms of their economies, cultures, politics, and societies worldwide.'), " +
                "('What are human rights?', 'Human rights are the fundamental rights and freedoms that individuals are entitled to under the law and society, such as freedom of speech, freedom of religion, and the right to life.'), " +
                "('What is DNA?', 'DNA, or deoxyribonucleic acid, is the molecule that carries the genetic instructions in the cells of living organisms, determining their genetic characteristics.'), " +
                "('What is holographic technology?', 'Holographic technology is a technique that records and reproduces the three-dimensional shape and motion of objects, commonly used in holographic photography and holographic microscopy.'), " +
                "('What is the Human Genome Project?', 'The Human Genome Project is a scientific research project aimed at identifying and sequencing all the genes of the human genome.'), " +
                "('What is cloud computing?', 'Cloud computing is the delivery of computing services over the Internet, including storage, databases, software, etc., allowing users to use and pay for services on-demand.'), " +
                "('What is machine learning?', 'Machine learning is an artificial intelligence technology that improves performance by allowing computers to learn patterns and rules from data.'), " +
                "('What is virtual reality?', 'Virtual reality is a computer-generated three-dimensional environment that simulates a real environment and allows users to interact with it.'), " +
                "('What is solar energy?', 'Solar energy refers to energy from the sun that can be converted into electricity, heat, etc., for human life and industrial production.'), " +
                "('What is AI ethics?', 'AI ethics is the study of how artificial intelligence systems should be used and designed to comply with ethical principles and values.'), " +
                "('What is quantum computing?', 'Quantum computing is a computational technique that uses quantum bits and quantum parallelism to process information.'), " +
                "('What is sustainable economy?', 'Sustainable economy refers to an economic model that achieves economic growth and social development without depleting natural resources and disrupting ecological balance.'), " +
                "('What is population aging?', 'Population aging refers to the increasing proportion of elderly population relative to other age groups in society.'), " +
                "('What is space exploration?', 'Space exploration is the activity of exploring outer space and studying the universe, planets, etc., using spacecraft.'), " +
                "('What is genetic engineering?', 'Genetic engineering is the technology of modifying the genetic material of organisms to alter their traits.'), " +
                "('What is big data?', 'Big data refers to large and complex datasets that are difficult to capture, manage, and process with traditional software tools.'), " +
                "('What is biodiversity?', 'Biodiversity refers to the variety and abundance of different species on Earth, including species diversity, genetic diversity, and ecosystem diversity.'), " +
                "('What is blockchain?', 'Blockchain is a decentralized database technology that records transaction data in a distributed manner, ensuring the security and transparency of data.'), " +
                "('What are applications of artificial intelligence?', 'Applications of artificial intelligence refer to the use of AI technology in various fields such as healthcare, finance, transportation, etc., to improve efficiency and accuracy.'), " +
                "('What is social networking?', 'Social networking is a network system composed of social connections and relationships between individuals, such as social media platforms.'), " +
                "('What is new energy?', 'New energy refers to energy forms that replace traditional energy sources and are environmentally friendly, such as solar energy, wind energy, etc.'), " +
                "('What is convolutional neural network?', 'Convolutional neural network is a special type of neural network structure that includes convolutional layers and pooling layers, widely used in image recognition and computer vision.'), " +
                "('什么是人工智能？', '人工智能是模拟人类智能过程的计算机系统，它可以执行像学习、推理、感知、理解和互动等任务。'), " +
                "('什么是机器学习？', '机器学习是人工智能的一个子领域，它研究如何设计和构建能够从数据中学习的系统，而无需明确编程。'), " +
                "('什么是深度学习？', '深度学习是机器学习的一个分支，它使用人工神经网络来模拟和学习复杂'), " +
                "('什么是自然语言处理？', '自然语言处理是人工智能的一个领域，它涉及计算机与人类语言之间的交互，包括文本分析、语音识别和语言翻译等。'), " +
                "('什么是数据挖掘？', '数据挖掘是从大量数据中发现有用模式和信息的过程，它涉及统计分析、机器学习和数据库技术等。'), " +
                "('什么是强化学习？', '强化学习是机器学习的一个分支，它研究如何通过观察环境和采取行动来达到某个目标，并从这个过程中学习。'), " +
                "('什么是神经网络？', '神经网络是一种模仿生物神经网络的计算系统，它由大量相互连接的人工神经元组成，用于模拟人脑的学习过程。'), " +
                "('什么是核能？', '核能是一种从原子核反应中释放出来的能量，常用于发电和医学应用。'), " +
                "('什么是全球气候变化？', '全球气候变化是指地球气候系统长期的变化，通常由人类活动导致的大气中温室气体的增加引起。'), " +
                "('什么是太阳能？', '太阳能是来自太阳的能量，通过光线和热量被转化为可用的电能或热能，被广泛应用于可再生能源领域。'), " +
                "('什么是人类基因组？', '人类基因组是人类细胞中包含的所有基因的总体遗传物质，它决定了人类的遗传特征和生物功能。'), " +
                "('什么是量子计算？', '量子计算是利用量子力学原理设计的计算系统，可以实现比传统计算机更快速和更复杂的计算。'), " +
                "('什么是人工生命？', '人工生命是一种通过模拟生物生命过程和行为来创建人造生命形式的研究领域。'), " +
                "('什么是机器人技术？', '机器人技术是研究和开发用于执行特定任务的自动化机械装置和系统的领域。'), " +
                "('什么是智能交通系统？', '智能交通系统是利用先进的信息和通信技术来管理和优化交通流量，提高交通效率和安全性的系统。'), " +
                "('什么是气候变化？', '气候变化指地球气候长期变化的过程，通常由人类活动导致的大气温室气体排放引起的。')," +
                "('什么是可持续发展？', '可持续发展是满足当前世代需求而不损害后代满足其需求的发展方式，注重经济、社会和环境之间的平衡。')," +
                "('什么是全球化？', '全球化是指世界范围内各国经济、文化、政治和社会等方面的相互联系和依存加深的过程。')," +
                "('什么是人权？', '人权是指个人在法律和社会中享有的基本权利和自由，如言论自由、宗教信仰自由、生存权等。')," +
                "('什么是DNA？', 'DNA是脱氧核糖核酸，是生物体细胞中携带遗传信息的分子，决定了生物的遗传特征。')," +
                "('什么是全息技术？', '全息技术是一种能够记录和重现物体的三维形态和运动的技术，常用于全息照相和全息显微镜中。')," +
                "('什么是人类基因组计划？', '人类基因组计划是一项旨在确定并解析人类所有基因组的科学研究项目。')," +
                "('什么是人工生命？', '人工生命是指通过计算机模拟或合成生命现象，以研究生命起源、进化等问题的科学领域。')," +
                "('什么是云计算？', '云计算是通过网络提供计算服务，包括存储、数据库、软件等，用户可以根据需要按需使用并支付费用。')," +
                "('什么是机器学习？', '机器学习是一种人工智能技术，通过让计算机学习数据模式和规律，从而不断改进性能。')," +
                "('什么是虚拟现实？', '虚拟现实是一种模拟真实环境并使用户可以与之交互的计算机生成的三维环境。')," +
                "('什么是太阳能？', '太阳能是指来自太阳的能量，可转化为电能、热能等用于人类生活和工业生产。')," +
                "('什么是人工智能伦理？', '人工智能伦理是研究人工智能系统应如何使用和设计以遵守道德准则和价值观的学科。')," +
                "('什么是量子计算？', '量子计算是利用量子比特的量子态和量子并行性来进行信息处理的计算方法。')," +
                "('什么是永续经济？', '永续经济是指在不破坏自然资源和生态平衡的前提下实现经济增长和社会发展的经济模式。')," +
                "('什么是人口老龄化？', '人口老龄化是指社会中老年人口比例增加，相对于其他年龄群体比例上升的现象。')," +
                "('什么是太空探索？', '太空探索是人类利用航天器探索太空并研究宇宙、行星等的活动。')," +
                "('什么是基因工程？', '基因工程是通过改变生物体的遗传物质来改变其性状的技术。')," +
                "('什么是大数据？', '大数据是指规模巨大且难以通过传统软件工具进行捕捉、管理和处理的数据集合。')," +
                "('什么是生物多样性？', '生物多样性是指地球上各种生物种类的丰富程度和多样性，包括物种多样性、基因多样性和生态系统多样性。')," +
                "('什么是区块链？', '区块链是一种去中心化的数据库技术，通过分布式的方式记录交易数据，保证数据的安全性和透明性。')," +
                "('什么是人工智能应用？', '人工智能应用是指将人工智能技术应用到各个领域，如医疗、金融、交通等，以提高效率和精度。')," +
                "('什么是社会网络？', '社会网络是由个体之间的社会联系和关系构成的网络系统，如社交媒体平台。')," +
                "('什么是新能源？', '新能源是指替代传统能源且对环境友好的能源形式，如太阳能、风能等。')," +
                "('什么是卷积神经网络？', '卷积神经网络是一种特殊的神经网络结构，它包含卷积层和池化层，广泛应用于图像识别和计算机视觉等领域。'), " +
                "('什么是循环神经网络？', '循环神经网络是一种具有循环连接的神经网络，它可以处理序列数据并具有记忆能力，适用于自然语言处理和时间序列预测等任务。'), " +
                "('What is the capital of France?', 'The capital of France is Paris.'), " +
                "('What is the currency of Japan?', 'The currency of Japan is the Japanese Yen.'), " +
                "('Who painted the Mona Lisa?', 'The Mona Lisa was painted by Leonardo da Vinci.'), " +
                "('What is the boiling point of water in Celsius?', 'The boiling point of water in Celsius is 100 degrees.'), " +
                "('Who wrote the play Romeo and Juliet?', 'Romeo and Juliet was written by William Shakespeare.'), " +
                "('What is the tallest mountain in the world?', 'The tallest mountain in the world is Mount Everest.'), " +
                "('What is the capital of Australia?', 'The capital of Australia is Canberra.'), " +
                "('What is the largest ocean on Earth?', 'The largest ocean on Earth is the Pacific Ocean.'), " +
                "('Who is the author of Harry Potter series?', 'The author of the Harry Potter series is J.K. Rowling.'), " +
                "('What is the chemical symbol for water?', 'The chemical symbol for water is H2O.'), " +
                "('What is the largest mammal on Earth?', 'The largest mammal on Earth is the blue whale.'), " +
                "('Who discovered penicillin?', 'Penicillin was discovered by Alexander Fleming.'), " +
                "('What is the capital of China?', 'The capital of China is Beijing.'), " +
                "('What is the national animal of Canada?', 'The national animal of Canada is the beaver.'), " +
                "('What is the speed of light in a vacuum?', 'The speed of light in a vacuum is approximately 299,792 kilometers per second.'), " +
                "('Who was the first man to step on the moon?', 'The first man to step on the moon was Neil Armstrong.'), " +
                "('What is the chemical symbol for gold?', 'The chemical symbol for gold is Au.'), " +
                "('What is the largest desert in the world?', 'The largest desert in the world is the Sahara Desert.'), " +
                "('Who wrote the famous novel Pride and Prejudice?', 'Pride and Prejudice was written by Jane Austen.'), " +
                "('What is the tallest animal in the world?', 'The tallest animal in the world is the giraffe.'), " +
                "('What is the capital of Brazil?', 'The capital of Brazil is Brasília.'), " +
                "('What is the chemical symbol for oxygen?', 'The chemical symbol for oxygen is O.'), " +
                "('Who painted The Starry Night?', 'The Starry Night was painted by Vincent van Gogh.'), " +
                "('What is the longest river in the world?', 'The longest river in the world is the Nile River.'), " +
                "('Who wrote the novel To Kill a Mockingbird?', 'To Kill a Mockingbird was written by Harper Lee.'), " +
                "('What is the chemical symbol for carbon?', 'The chemical symbol for carbon is C.'), " +
                "('What is the largest planet in our solar system?', 'The largest planet in our solar system is Jupiter.'), " +
                "('Who is known as the father of modern physics?', 'Albert Einstein is known as the father of modern physics.'), " +
                "('What is the capital of Russia?', 'The capital of Russia is Moscow.'), " +
                "('What is the chemical symbol for sodium?', 'The chemical symbol for sodium is Na.'), " +
                "('Who painted the ceiling of the Sistine Chapel?', 'The ceiling of the Sistine Chapel was painted by Michelangelo.'), " +
                "('What is the largest bird in the world?', 'The largest bird in the world is the ostrich.'), " +
                "('What is the capital of India?', 'The capital of India is New Delhi.'), " +
                "('What is the chemical symbol for silver?', 'The chemical symbol for silver is Ag.'), " +
                "('Who wrote the novel 1984?', '1984 was written by George Orwell.'), " +
                "('What is the smallest country in the world?', 'The smallest country in the world is Vatican City.'), " +
                "('What is the chemical symbol for iron?', 'The chemical symbol for iron is Fe.'), " +
                "('Who is known as the father of modern psychology?', 'Wilhelm Wundt is known as the father of modern psychology.'), " +
                "('What is the capital of South Africa?', 'The capital of South Africa is Pretoria.'), " +
                "('What is the chemical symbol for calcium?', 'The chemical symbol for calcium is Ca.'), " +
                "('Who wrote the novel Moby-Dick?', 'Moby-Dick was written by Herman Melville.'), " +
                "('What is the chemical symbol for hydrogen?', 'The chemical symbol for hydrogen is H.'), " +
                "('What is the largest mammal on land?', 'The largest mammal on land is the African elephant.'), " +
                "('What is the capital of Italy?', 'The capital of Italy is Rome.'), " +
                "('What is the chemical symbol for lead?', 'The chemical symbol for lead is Pb.'), " +
                "('Who wrote the novel The Great Gatsby?', 'The Great Gatsby was written by F. Scott Fitzgerald.'), " +
                "('What is the chemical symbol for nitrogen?', 'The chemical symbol for nitrogen is N.'), " +
                "('Who painted The Last Supper?', 'The Last Supper was painted by Leonardo da Vinci.'), " +
                "('What is the highest mountain in North America?', 'The highest mountain in North America is Denali (Mount McKinley).'), " +
                "('What is the chemical symbol for tin?', 'The chemical symbol for tin is Sn.'), " +
                "('Who wrote the novel War and Peace?', 'War and Peace was written by Leo Tolstoy.'), " +
                "('What is the chemical symbol for mercury?', 'The chemical symbol for mercury is Hg.'), " +
                "('What is the most abundant gas in Earth''s atmosphere?', 'The most abundant gas in Earth''s atmosphere is nitrogen.'), " +
                "('What is the chemical symbol for silicon?', 'The chemical symbol for silicon is Si.'), " +
                "('Who painted The Persistence of Memory?', 'The Persistence of Memory was painted by Salvador Dalí.'), " +
                "('What is the capital of Brazil?', 'The capital of Brazil is Brasília.'), " +
                "('Who is the current president of the United States?', 'The current president of the United States is Joe Biden.'), " +
                "('What is the chemical formula of water?', 'The chemical formula of water is H2O.'), " +
                "('Who wrote the play Hamlet?', 'Hamlet was written by William Shakespeare.'), " +
                "('What is the highest mountain peak in the world?', 'The highest mountain peak in the world is Mount Everest.'), " +
                "('What is the scientific name of the human species?', 'The scientific name of the human species is Homo sapiens.'), " +
                "('Who is the author of the book To Kill a Mockingbird?', 'To Kill a Mockingbird was written by Harper Lee.'), " +
                "('What is the melting point of ice in Celsius?', 'The melting point of ice in Celsius is 0 degrees.'), " +
                "('Who painted the famous artwork Starry Night?', 'The Starry Night was painted by Vincent van Gogh.'), " +
                "('What is the currency of the United Kingdom?', 'The currency of the United Kingdom is the British Pound.'), " +
                "('Who is the founder of Microsoft Corporation?', 'The founder of Microsoft Corporation is Bill Gates.'), " +
                "('What is the chemical symbol for gold?', 'The chemical symbol for gold is Au.'), " +
                "('Who wrote the novel The Catcher in the Rye?', 'The Catcher in the Rye was written by J.D. Salinger.'), " +
                "('What is the largest planet in our solar system?', 'The largest planet in our solar system is Jupiter.'), " +
                "('Who discovered the theory of relativity?', 'The theory of relativity was discovered by Albert Einstein.'), " +
                "('What is the chemical symbol for iron?', 'The chemical symbol for iron is Fe.'), " +
                "('Who painted the ceiling of the Sistine Chapel?', 'The ceiling of the Sistine Chapel was painted by Michelangelo.'), " +
                "('What is the capital city of Japan?', 'The capital city of Japan is Tokyo.'), " +
                "('Who is the author of The Lord of the Rings series?', 'The author of The Lord of the Rings series is J.R.R. Tolkien.'), " +
                "('What is the atomic number of oxygen?', 'The atomic number of oxygen is 8.'), " +
                "('Who composed the famous musical piece Symphony No. 9?', 'Symphony No. 9 was composed by Ludwig van Beethoven.'), " +
                "('What is the most spoken language in the world?', 'The most spoken language in the world is Mandarin Chinese.'), " +
                "('Who is the CEO of Facebook?', 'The CEO of Facebook is Mark Zuckerberg.'), " +
                "('What is the chemical symbol for silver?', 'The chemical symbol for silver is Ag.'), " +
                "('Who wrote the novel Pride and Prejudice?', 'Pride and Prejudice was written by Jane Austen.'), " +
                "('What is the boiling point of water in Fahrenheit?', 'The boiling point of water in Fahrenheit is 212 degrees.'), " +
                "('Who is the director of the movie Inception?', 'The director of the movie Inception is Christopher Nolan.'), " +
                "('What is the chemical symbol for carbon?', 'The chemical symbol for carbon is C.'), " +
                "('Who was the first woman to win a Nobel Prize?', 'The first woman to win a Nobel Prize was Marie Curie.'), " +
                "('What is the currency of Germany?', 'The currency of Germany is the Euro.'), " +
                "('Who is the founder of Amazon.com?', 'The founder of Amazon.com is Jeff Bezos.'), " +
                "('What is the tallest building in the world?', 'The tallest building in the world is the Burj Khalifa in Dubai, United Arab Emirates.'), " +
                "('Who wrote the play Macbeth?', 'Macbeth was written by William Shakespeare.'), " +
                "('What is the chemical symbol for sodium?', 'The chemical symbol for sodium is Na.'), " +
                "('Who is the lead vocalist of the band Queen?', 'The lead vocalist of the band Queen is Freddie Mercury.'), " +
                "('What is the capital city of Australia?', 'The capital city of Australia is Canberra.'), " +
                "('Who is the CEO of Tesla, Inc.?', 'The CEO of Tesla, Inc. is Elon Musk.'), " +
                "('What is the chemical symbol for helium?', 'The chemical symbol for helium is He.'), " +
                "('Who wrote the novel The Great Gatsby?', 'The Great Gatsby was written by F. Scott Fitzgerald.'), " +
                "('What is the largest ocean on Earth?', 'The largest ocean on Earth is the Pacific Ocean.'), " +
                "('Who is the creator of the TV series Breaking Bad?', 'The creator of the TV series Breaking Bad is Vince Gilligan.'), " +
                "('What is the chemical symbol for potassium?', 'The chemical symbol for potassium is K.'), " +
                "('Who is the current Secretary-General of the United Nations?', 'The current Secretary-General of the United Nations is António Guterres.'), " +
                "('What is the chemical symbol for nitrogen?', 'The chemical symbol for nitrogen is N.'), " +
                "('Who painted the famous artwork The Persistence of Memory?', 'The Persistence of Memory was painted by Salvador Dalí.'), " +
                "('What is the largest continent by land area?', 'The largest continent by land area is Asia.'), " +
                "('Who is the author of the novel The Hitchhiker''s Guide to the Galaxy?', 'The author of the novel The Hitchhiker''s Guide to the Galaxy is Douglas Adams.'), " +
                "('What is chemical symbol for copper?', 'The chemical symbol for copper is Cu.'), " +
                "('Who directed the movie The Shawshank Redemption?', 'The movie The Shawshank Redemption was directed by Frank Darabont.'), " +
                "('What is capital city of France?', 'The capital city of France is Paris.'), " +
                "('Who wrote the play A Streetcar Named Desire?', 'A Streetcar Named Desire was written by Tennessee Williams.'), " +
                "('What is chemical symbol for uranium?', 'The chemical symbol for uranium is U.'), " +
                "('什么是生成对抗网络？', '生成对抗网络是一种由生成器和判别器组成的网络结构，它们相互竞争并共同提高，被用于生成逼真的图像和数据。');"
        );
    }
}
