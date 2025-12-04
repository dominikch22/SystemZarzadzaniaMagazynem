git checkout master
git pull origin master
git checkout prod
git merge master
./gradlew bootJar
sudo systemctl restart magazyn