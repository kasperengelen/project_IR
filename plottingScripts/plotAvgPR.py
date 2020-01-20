import matplotlib.pyplot as plt
plt.axis([0, 1, 0, 1])

# files = ["tagQuery/tagsQueryResultsBM25.txt",
#          "tagQuery/tagsQueryResultsTD-IDF.txt",
#          "tagQuery/tagsQueryResultsLMD.txt",
#          "tagQuery/tagsQueryResultsLMJM.txt"]

# AVG. RANK BM25 : 7.365815738000903
# AVG. RANK TF-IDF : 7.650049288832559
# AVG. RANK LM Dirichlet : 7.450248348889253
# AVG. RANK LM Jelinek-Mercer : 7.415525595112703

files = ["titleTermQuery/titleTermQueryResultsBM25.txt",
         "titleTermQuery/titleTermQueryResultsTD-IDF.txt",
         "titleTermQuery/titleTermQueryResultsLMD.txt",
         "titleTermQuery/titleTermQueryResultsLMJM.txt"]

# AVG. RANK BM25 : 7.267755203789107
# AVG. RANK TF-IDF : 7.1945450127566515
# AVG. RANK LM Dirichlet : 7.194619945567652
# AVG. RANK LM Jelinek-Mercer : 7.192645611306218


sims = ["BM25", "TF-IDF", "LM Dirichlet", "LM Jelinek-Mercer"]

linestyles = ['-', '-', '-.', ':']

if __name__ == "__main__":

    for i in range(len(files)):
        f = open(files[i], "r")

        p_at_k = {i: [] for i in range(20)}
        r_at_k = {i: [] for i in range(20)}

        # P = TP/(TP+FP)
        # R = TP/(TP+FN)
        # rel_doc_num = TP + FN

        avg_acc = 0
        rel_count = 0

        for line in f:
            TP, FP = 0, 0
            rel_doc_num, top20vector = line.strip("\n").split("|")
            rel_doc_num = int(rel_doc_num)
            if len(top20vector) < 20:
                top20vector += ("0"*(20-len(top20vector)))

            for k in range(20):
                relevant = bool(int(top20vector[k]))
                TP += int(relevant)
                FP += int(not relevant)
                P_AT_K = TP / (TP + FP)
                R_AT_K = TP / rel_doc_num
                p_at_k[k].append(P_AT_K)
                r_at_k[k].append(R_AT_K)
                if relevant:
                    avg_acc += k
                    rel_count += 1

        print("AVG. RANK {} : {}".format(sims[i], avg_acc/rel_count))

        xaxis = []  # recall
        yaxis = []  # precision

        for k, vals in p_at_k.items():
            avg = sum(vals) / len(vals)
            yaxis.append(avg)
            # print("AVG P@{} : {}".format(k+1, avg))


        for k, vals in r_at_k.items():
            avg = sum(vals) / len(vals)
            xaxis.append(avg)

        line, = plt.plot(xaxis, yaxis, linestyle=linestyles[i])
        line.set_label(sims[i])

    plt.xlabel("recall")
    plt.ylabel("precision")
    plt.legend()
    plt.show()
