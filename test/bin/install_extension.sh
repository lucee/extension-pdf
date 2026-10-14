#!/usr/bin/env bash
set -e

script_dir=$(cd `dirname $0` && pwd)
repo_root=${script_dir}/../../

cd "${repo_root}test"
lucee_home=$(box server info property=serverHomeDirectory)

if [ -z "${lucee_home}" ]; then
  echo "ERROR: Could not find Lucee home directory"
  exit 1
fi

deploy_dir=${lucee_home}/WEB-INF/lucee-server/deploy

echo "Found Lucee home directory at ${lucee_home}"
echo "Copying the following files to ${deploy_dir}:"
ls "${repo_root}"dist/*.lex

cp "${repo_root}"dist/*.lex "${deploy_dir}"

echo "Waiting for Lucee to install the extension..."
while ls "${deploy_dir}"/*.lex &> /dev/null; do
  sleep 1
  echo -n .
done
echo
echo "Extension installed"
