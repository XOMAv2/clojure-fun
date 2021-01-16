import sys
import os

def func(old, new):
    services = []
    
    for filename in os.listdir('.'):
        if os.path.isdir(filename) and filename.endswith('-service'):
            services.append(filename)
            
    for service_folder in services:
        filedata = None
        
        with open(f'./{service_folder}/config.edn', 'r') as file:
          filedata = file.read()

        filedata = filedata.replace(old, new)

        with open(f'./{service_folder}/config.edn', 'w') as file:
          file.write(filedata)
            

if __name__ == '__main__':
    if (len(sys.argv[1:]) == 0):
        print('кря')
    else:
        env_type = sys.argv[1:][0]
        local_env = ':env-type :local'
        heroku_env = ':env-type :heroku'
        
        if (env_type == 'local' or env_type == 'l'):
            func(heroku_env, local_env)
            print('Local environment done.')
        elif (env_type == 'heroku' or env_type == 'h'):
            func(local_env, heroku_env)
            print('Heroku environment done.')
        else:
            print('кря')