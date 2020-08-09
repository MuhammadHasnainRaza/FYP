import React from 'react';
import { StyleSheet, Text, View, ActivityIndicator } from 'react-native';
import * as firebase from 'firebase';


export default class Loading extends React.Component{
    static navigationOptions = {
    header:null
  }

  componentDidMount(){
    this.unsuscribeAuth = firebase.auth().onAuthStateChanged((user)=>{
        if(user){
            this.props.navigation.navigate("Home")
        }else{
            this.props.navigation.navigate("Login")
        }
    })
  }
  componentWillUnmount(){
    this.unsuscribeAuth()
}
   
    render(){
        return (
            <View style={styles.container}>
                <ActivityIndicator size="large" color="#d9534f"/>
            </View>
          );
    }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    justifyContent: "center",

  },
});
