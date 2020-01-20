import matplotlib.pyplot as plt
# plt.axis([0, 1, 0, 1])

files = ["rocchioTitleTermQueryResultsBM25.txt"]
sims = ["BM25"]


if __name__ == "__main__":

    for i in range(len(files)):
        f = open(files[i], "r")

        p_at_k1 = {i: [] for i in range(20)}
        r_at_k1 = {i: [] for i in range(20)}
        p_at_k2 = {i: [] for i in range(20)}
        r_at_k2 = {i: [] for i in range(20)}

        # P = TP/(TP+FP)
        # R = TP/(TP+FN)
        # rel_doc_num = TP + FN
        for line in f:
            TP, FP = 0, 0
            rel_doc_num, top20vectorBefore, top20vectorAfter = line.strip("\n").split("|")
            rel_doc_num = int(rel_doc_num)
            if len(top20vectorBefore) < 20:
                top20vectorBefore += ("0"*(20-len(top20vectorBefore)))
            if len(top20vectorAfter) < 20:
                top20vectorAfter += ("0"*(20-len(top20vectorAfter)))

            for k in range(20):
                relevant = bool(int(top20vectorBefore[k]))
                TP += int(relevant)
                FP += int(not relevant)
                P_AT_K = TP / (TP + FP)
                R_AT_K = TP / rel_doc_num
                p_at_k1[k].append(P_AT_K)
                r_at_k1[k].append(R_AT_K)

            for k in range(20):
                relevant = bool(int(top20vectorAfter[k]))
                TP += int(relevant)
                FP += int(not relevant)
                P_AT_K = TP / (TP + FP)
                R_AT_K = TP / rel_doc_num
                p_at_k2[k].append(P_AT_K)
                r_at_k2[k].append(R_AT_K)


        xaxis1 = []  # recall
        yaxis1 = []  # precision
        xaxis2 = []  # recall
        yaxis2 = []  # precision

        for k, vals in p_at_k1.items():
            avg = sum(vals) / len(vals)
            yaxis1.append(avg)

        for k, vals in r_at_k1.items():
            avg = sum(vals) / len(vals)
            xaxis1.append(avg)

        for k, vals in p_at_k2.items():
            avg = sum(vals) / len(vals)
            yaxis2.append(avg)

        for k, vals in r_at_k2.items():
            avg = sum(vals) / len(vals)
            xaxis2.append(avg)

        line1, = plt.plot(xaxis1, yaxis1)
        line2, = plt.plot(xaxis2, yaxis2)
        line1.set_label("Before")
        line2.set_label("After")

    plt.xlabel("recall")
    plt.ylabel("precision")
    plt.legend()
    plt.show()
