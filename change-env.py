import sys
import os

def replace_text_in_file(path, old, new):
    filedata = None
        
    with open(path, 'r') as file:
        filedata = file.read()

    filedata = filedata.replace(old, new)

    with open(path, 'w') as file:
        file.write(filedata)

def change_env(to_local):
    services = []
    port_map = {'warranty': '8180',
                'warehouse': '8280',
                'order': '8380',
                'store': '8480',
                'session': '8580'}
    port_map = {} # Чтобы не трогать Dockerfile'ы.
    local_env = ':env-type :local'
    heroku_env = ':env-type :heroku'
    
    for filename in os.listdir('.'):
        if os.path.isdir(filename) and filename.endswith('-service'):
            services.append(filename)
            
    for service_folder in services:
        congig_path = f'./{service_folder}/config.edn'
        dockerfile_path = f'./{service_folder}/Dockerfile'
        service = service_folder.replace('-service', '')

        if (to_local):
            replace_text_in_file(congig_path, heroku_env, local_env)

            if (service in port_map):
                replace_text_in_file(dockerfile_path, 'CMD', f'EXPOSE {port_map[service]}\nCMD')
                replace_text_in_file(dockerfile_path, '0.0.0.0 $PORT', f'127.0.0.1 {port_map[service]}')
        else:
            replace_text_in_file(congig_path, local_env, heroku_env)
            
            if (service in port_map):
                replace_text_in_file(dockerfile_path, f'EXPOSE {port_map[service]}\nCMD', 'CMD')
                replace_text_in_file(dockerfile_path, f'127.0.0.1 {port_map[service]}', '0.0.0.0 $PORT')

if __name__ == '__main__':
    if (len(sys.argv[1:]) == 0):
        print('кря')
    else:
        env_type = sys.argv[1:][0]
        local_env = ':env-type :local'
        heroku_env = ':env-type :heroku'
        
        if (env_type == 'local' or env_type == 'l'):
            change_env(True)
            print('Local environment done.')
        elif (env_type == 'heroku' or env_type == 'h'):
            change_env(False)
            print('Heroku environment done.')
        else:
            print('кря')