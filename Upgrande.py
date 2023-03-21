import os
import subprocess


# 以当前文件所在目录为基础，在该目录下创建一个名为"example"的目录
path = "/Users/mac/IdeaProjects/demo2/target/dependency"
os.makedirs(path, exist_ok=True)

os.chdir('/Users/mac/IdeaProjects/demo2/target/dependency')
result = os.system("jar -xf /Users/mac/IdeaProjects/demo2/target/*.jar")
print("jar -xf : "+ ('complete'if(result == 0) else 'failed'))


result = os.system("scp -r BOOT-INF/classes lq_nyb@203.189.0.62:~/ReminderUpdate")
result += os.system("scp -r BOOT-INF/lib lq_nyb@203.189.0.62:~/ReminderUpdate")
print("scp : "+ ('complete'if(result == 0) else 'failed'))
