import React from 'react';
import { StyleSheet, Text, View, Image, TouchableOpacity, Alert, ImageBackground} from 'react-native';
import {  Item, Input, Label, Button } from 'native-base';
import * as firebase from 'firebase';

export default class LoginScreen extends React.Component{
  state={
    email:"",
    password:""
  }

  static navigationOptions = {
    title:"Admin Panel",
    headerStyle: {
       backgroundColor: '#696969',
      }
  }
  
 userSignin(email,pass){
    firebase.auth().signInWithEmailAndPassword(email,pass)
    .then(()=>{
    this.props.navigation.replace("home")
    })
    .catch(error=>{
      Alert.alert(error.message)
    })
  }
 
    render(){
        return (
            <View style={styles.container}>
            <View>
                <Image source={require("../assets/0.png")}
                 style={{ width: 350, height: 200 }}
                   
                />
                
              </View>
              

      
              

              <Item floatingLabel>
                <Label>Email Id</Label>
               <Input
                value={this.state.email} 
                onChangeText={(text)=>this.setState({email:text})}
                />
              </Item>
              <Item floatingLabel>
                <Label>Password</Label>
               <Input 
                 secureTextEntry={true}
                 value={this.state.password}
                 onChangeText={(text)=>this.setState({password:text})}
               />
              </Item>
              
              <View>
              <Button full rounded danger
              style={{margin:10,justifyContent:"center",backgroundColor:"#696969"}}
              onPress={()=>this.userSignin(this.state.email,this.state.password)}>
                <Text style={{fontSize:22,color:"white"}}>Login</Text>
              </Button>
              </View>

              <TouchableOpacity
              onPress={()=>this.props.navigation.navigate("Signup")}>
                <Text style={{textAlign:"center"}}></Text>
              </TouchableOpacity>

            </View>
          );
    }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#a9a9a9',
    justifyContent: "flex-start",
    padding:13
  },
});
