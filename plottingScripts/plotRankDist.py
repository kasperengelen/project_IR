import matplotlib.pyplot as plt

# files = ["titleQueryRankBM25.txt", "titleQueryRankTD-IDF.txt", "titleQueryRankLMD.txt", "titleQueryRankLMJM.txt"]
files = ["titleQuery/titleQueryRankBM25.txt", "titleQuery/titleQueryRankTD-IDF.txt", "titleQuery/titleQueryRankLMD.txt", "titleQuery/titleQueryRankLMJM.txt"]
sims = ["BM25", "TF-IDF", "LM Dirichlet", "LM Jelinek-Mercer"]


if __name__ == "__main__":

    values = {}
    counts = {}
    overlap = [1 for _ in range(500000)]
    top_overlap = [1 for _ in range(500000)]

    for i in range(len(files)):
        f = open(files[i], "r")
        vals = []
        count = 0
        line_count = 0
        for line in f:
            rank = int(line)
            if rank > 0:
                count += 1
                vals.append(rank)
            else:
                top_overlap[line_count] = 0 if sims[i] != "TF-IDF" else 1
                overlap[line_count] = 0
            line_count += 1
        values[sims[i]] = vals
        counts[sims[i]] = count
        print("{}".format(sum(vals)/count))

        # plt.title(sims[i])
        # plt.hist()
        # plt.show()

    fig, ax = plt.subplots()
    ax.boxplot(values.values(), vert=False)
    ax.set_yticklabels(values.keys())
    ax.xaxis.set_ticks(range(1, 21))
    plt.xlabel('rank')
    plt.show()

    fig, ax = plt.subplots()
    plt.hist(values.values(), bins=range(1, 22), histtype="bar", label=sims)
    ax.xaxis.set_ticks(range(1, 21))
    ax.legend(prop={'size': 10})
    plt.xlabel('rank')
    plt.ylabel('# of documents')
    plt.show()

    for m, c in counts.items():
        print("{} : {}/500K = {}%".format(m, c, (c/500000)*100))

    print("{}% found by all models.".format((sum(overlap)/500000)*100))
    print("{}% found by best models.".format((sum(top_overlap)/500000)*100))
