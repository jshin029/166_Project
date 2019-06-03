fd = open('create.sql','r')
query = ""

for line in fd:
    query += str(line)
